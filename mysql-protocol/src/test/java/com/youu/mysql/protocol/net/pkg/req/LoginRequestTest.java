package com.youu.mysql.protocol.net.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class LoginRequestTest extends TestCase {
    LoginRequest packet = new LoginRequest();

    @Test
    public void testRead1() {
        String hexDump
            =
            "bc00000185a67f0000000001210000000000000000000000000000000000000000000000726f6f740014a5e89d16a0527e1b9352517f2b79e7f1e8afdfa56d7973716c5f6e61746976655f70617373776f7264006b035f6f730a6d61636f7331302e31320c5f636c69656e745f6e616d65086c69626d7973716c045f7069640538323736320f5f636c69656e745f76657273696f6e06352e362e3337095f706c6174666f726d067838365f36340c70726f6772616d5f6e616d65056d7973716c";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        LoginRequest read = (LoginRequest)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(1024);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead2() {
        String hexDump
            =
            "c20000018da67f0000000001210000000000000000000000000000000000000000000000726f6f7400141125517c5323ec90da52652a16e49822e155856c6d7973716c006d7973716c5f6e61746976655f70617373776f7264006b035f6f730a6d61636f7331302e31320c5f636c69656e745f6e616d65086c69626d7973716c045f7069640538323739360f5f636c69656e745f76657273696f6e06352e362e3337095f706c6174666f726d067838365f36340c70726f6772616d5f6e616d65056d7973716c";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        LoginRequest read = (LoginRequest)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(1024);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }
}