package com.youu.mysql.protocol.handler;

import java.security.DigestException;

import com.google.common.primitives.Bytes;
import com.mysql.cj.CharsetMapping;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.util.StringUtils;
import com.youu.mysql.protocol.pkg.req.ComQuery;
import com.youu.mysql.protocol.pkg.req.ComQuit;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.pkg.res.HandshakePacket;
import com.youu.mysql.protocol.pkg.res.OkPacket;
import com.youu.mysql.protocol.pkg.res.ResultSetPacket;
import com.youu.mysql.storage.StorageConfig;
import com.youu.mysql.storage.impl.H2StorageProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MySQLServerHandlerTest {
    private static final EmbeddedChannel channel = new EmbeddedChannel(new MySQLServerHandler(new H2StorageProvider()));

    @BeforeClass
    public static void init() throws DigestException {
        HandshakePacket handshakePacket = channel.readOutbound();
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
                    StringUtils.getBytes(StorageConfig.getConfig().getUserPass().get(loginRequest.getUsername()),
                        CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet())),
                    seed);
            loginRequest.setAuthPluginName("caching_sha2_password");
        } else {
            passes = Security.scramble411(StorageConfig.getConfig().getUserPass().get(loginRequest.getUsername()),
                seed,
                CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet()));
        }
        loginRequest.setAuthResponse(passes);
        channel.writeInbound(loginRequest);

        OkPacket response = channel.readOutbound();
        Assert.assertEquals(0xfe, response.getHeader() | 0xfe);

    }

    @AfterClass
    public static void destroy() {
        Assert.assertEquals(false, channel.isActive());
    }

    @Test
    public void test1_channelActive() {
        Assert.assertEquals(true, channel.isActive());
    }

    @Test
    public void test2_channelRead0() {
        channel.writeInbound(new ComQuery("select 1"));
        ResultSetPacket response = channel.readOutbound();
        Assert.assertNotNull(response);

        channel.writeInbound(new ComQuery("create schema test"));

        Assert.assertSame(OkPacket.class, channel.readOutbound().getClass());

        channel.writeInbound(new ComQuery("use test"));
        Assert.assertSame(OkPacket.class, channel.readOutbound().getClass());

        channel.writeInbound(new ComQuery("select @@version_comment"));
        response = channel.readOutbound();
        Assert.assertNotNull(response);
    }

    @Test
    public void test3_exceptionCaught() {
        channel.checkException();
    }

    @Test
    public void test4_channelInactive() {
        channel.writeInbound(new ComQuit());
    }

}