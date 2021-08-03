package com.youu.mysql.protocol.handler;

import java.net.URI;
import java.security.DigestException;

import com.google.common.primitives.Bytes;
import com.mysql.cj.CharsetMapping;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.util.StringUtils;
import com.youu.mysql.protocol.base.MySQLContainerBaseTest;
import com.youu.mysql.protocol.pkg.req.ComQuery;
import com.youu.mysql.protocol.pkg.req.ComQuit;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.pkg.res.HandshakePacket;
import com.youu.mysql.storage.StorageConfig;
import com.youu.mysql.storage.StorageConfig.HostPort;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.MySQLContainer;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MySQLServerDirectHandlerTest extends MySQLContainerBaseTest {
    private static EmbeddedChannel channel;

    @BeforeClass
    public static void init() throws DigestException {
        if (!localTest) {
            String jdbcUrl = MYSQL.getJdbcUrl();
            URI uri = URI.create(jdbcUrl.substring(5));
            StorageConfig.getConfig().setSchema(new HostPort(uri.getHost(), uri.getPort()));

            MySQLContainer store1 = new MySQLContainer<>(MYSQL_80_IMAGE)
                .withDatabaseName("test")
                .withUsername(USER_NAME)
                .withPassword(PASS_WORD);
            store1.start();

            jdbcUrl = store1.getJdbcUrl();
            uri = URI.create(jdbcUrl.substring(5));
            StorageConfig.getConfig().getStorages().clear();
            StorageConfig.getConfig().getStorages().add(new HostPort(uri.getHost(), uri.getPort()));
            //add a inaccessible store for test2_channelRead2
            StorageConfig.getConfig().getStorages().add(new HostPort("127.0.0.1", 10010));

        }
        channel = new EmbeddedChannel(new MySQLServerDirectHandler());
        ByteBuf handshakeData = channel.readOutbound();
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.read(handshakeData);
        byte[] authPluginDataPart1 = handshakePacket.getAuthPluginDataPart1();
        byte[] authPluginDataPart2 = handshakePacket.getAuthPluginDataPart2();
        byte[] authPluginDataPart2True = new byte[authPluginDataPart2.length - 1];
        System.arraycopy(authPluginDataPart2, 0, authPluginDataPart2True, 0, authPluginDataPart2True.length);
        byte[] seed = Bytes.concat(authPluginDataPart1, authPluginDataPart2True);
        String hexDump
            =
            "d900000107a23e01ffffff001c0000000000000000000000000000000000000000000000726f6f74001457a7fdd47410b2a244b2796d529bfa7afdd802946d7973716c5f6e61746976655f70617373776f72640088105f72756e74696d655f76657273696f6e09312e382e305f3233310f5f636c69656e745f76657273696f6e06382e302e32350f5f636c69656e745f6c6963656e73650347504c0f5f72756e74696d655f76656e646f72124f7261636c6520436f72706f726174696f6e0c5f636c69656e745f6e616d65114d7953514c20436f6e6e6563746f722f4a";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.read(buf);
        byte[] passes;
        if ("caching_sha2_password".equals(handshakePacket.getAuthPluginName())) {
            passes = Security
                .scrambleCachingSha2(
                    StringUtils.getBytes(PASS_WORD,
                        CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet())),
                    seed);
            loginRequest.setAuthPluginName("caching_sha2_password");
        } else {
            passes = Security.scramble411(PASS_WORD,
                seed,
                CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet()));
        }
        loginRequest.setAuthResponse(passes);
        channel.writeInbound(loginRequest);
        ByteBuf response = channel.readOutbound();
        if (response.getUnsignedByte(4) != 0x01) { Assert.assertEquals(0xfe, response.getUnsignedByte(4) | 0xfe); }

    }

    @Test
    public void test1_channelActive() {
        Assert.assertEquals(true, channel.isActive());
    }

    @Test
    public void test2_channelRead0() {
        channel.writeInbound(new ComQuery("select 1"));
        ByteBuf response = channel.readOutbound();
        Assert.assertEquals(
            "010000010117000002036465660000000131000c3f000100000008810000000002000003013107000004fe000002000000",
            ByteBufUtil.hexDump(response));
    }

    @Test
    public void test2_channelRead1() {
        channel.writeInbound(new ComQuery("/*+USE_STORE(1)*/select 1"));
        ByteBuf response = channel.readOutbound();
        Assert.assertEquals(
            "010000010117000002036465660000000131000c3f000100000008810000000002000003013107000004fe000002000000",
            ByteBufUtil.hexDump(response));
    }

    @Test
    public void test2_channelRead2() {
        channel.writeInbound(new ComQuery("/*+USE_STORE(2)*/select 1"));
        ByteBuf response = channel.readOutbound();
        Assert.assertEquals(0xff, response.getUnsignedByte(4));
    }

    @Test
    public void test2_channelRead3() {
        channel.writeInbound(new ComQuery("/*+USE_STORE(3)*/select 1"));
        ByteBuf response = channel.readOutbound();
        Assert.assertEquals(
            "2f000001ffe80323313053303048494e54206f66205553455f53544f52452073686f756c64206265747765656e205b312c325d",
            ByteBufUtil.hexDump(response));
    }

    @Test
    public void test3_exceptionCaught() {
        channel.checkException();
    }

    @Test
    public void test4_channelInactive() {
        channel.writeInbound(new ComQuit());
        channel.close();
        Assert.assertEquals(false, channel.isActive());
    }

}
