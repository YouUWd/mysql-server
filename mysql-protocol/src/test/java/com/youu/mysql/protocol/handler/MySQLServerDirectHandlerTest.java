package com.youu.mysql.protocol.handler;

import java.net.URI;
import java.security.DigestException;

import com.google.common.primitives.Bytes;
import com.mysql.cj.CharsetMapping;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.util.StringUtils;
import com.youu.mysql.protocol.MySQLContainerBaseTest;
import com.youu.mysql.protocol.pkg.req.ComQuery;
import com.youu.mysql.protocol.pkg.req.ComQuit;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.pkg.res.HandshakePacket;
import com.youu.mysql.protocol.pkg.res.ResultSetPacket;
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

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MySQLServerDirectHandlerTest extends MySQLContainerBaseTest {
    private static final EmbeddedChannel channel = new EmbeddedChannel(new MySQLServerDirectHandler());

    @BeforeClass
    public static void init() throws DigestException {
        String jdbcUrl = MYSQL.getJdbcUrl();
        URI uri = URI.create(jdbcUrl.substring(5));
        StorageConfig.getConfig().setSchema(new HostPort(uri.getHost(), uri.getPort()));
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
            "bc00000185a67f0000000001210000000000000000000000000000000000000000000000726f6f740014a5e89d16a0527e1b9352517f2b79e7f1e8afdfa56d7973716c5f6e61746976655f70617373776f7264006b035f6f730a6d61636f7331302e31320c5f636c69656e745f6e616d65086c69626d7973716c045f7069640538323736320f5f636c69656e745f76657273696f6e06352e362e3337095f706c6174666f726d067838365f36340c70726f6772616d5f6e616d65056d7973716c";
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
        Assert.assertEquals(0xfe, response.getUnsignedByte(4) | 0xfe);

    }

    @Test
    public void test1_channelActive() {
        Assert.assertEquals(true, channel.isActive());
    }

    @Test
    public void test2_channelRead0() {
        channel.writeInbound(new ComQuery("select 1"));
        ByteBuf response = channel.readOutbound();
        String s1 = ByteBufUtil.hexDump(response);
        ResultSetPacket packet = new ResultSetPacket();
        packet.read(response);
        ByteBuf buf = Unpooled.buffer(1024);
        packet.write(buf);
        Assert.assertEquals(s1, ByteBufUtil.hexDump(buf));
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
