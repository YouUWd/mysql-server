package com.youu.mysql.protocol.pkg.res;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class HandshakePacketTest extends TestCase {
    HandshakePacket packet = new HandshakePacket();

    @Test
    public void test() {
        short charset = 255;
        ByteBuf buf = Unpooled.buffer(1024);
        HandshakePacket packet = HandshakePacket.builder()
            .serverVersion("8.0.22-HTAP")
            .connectionId(1)
            .authPluginDataPart1(new byte[] {1, 2, 3, 4, 5, 6, 7, 8})
            .capabilityFlags1(0xffff)
            .characterSet(charset)
            .statusFlags(0x0002)
            .capabilityFlags2(0xffc7)
            .authPluginDataLength((short)21)
            .authPluginDataPart2(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13})
            .authPluginName("mysql_native_password")
            .build();
        packet.writePayload(buf);
        this.packet.readPayload(buf);
        System.out.println(this.packet);
        ByteBuf buf1 = Unpooled.buffer(1024);
        this.packet.writePayload(buf1);
        buf.readerIndex(0);
        Assert.assertEquals(ByteBufUtil.hexDump(buf), ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead1() {
        String hexDump
            =
            "700000000a352e372e31342d416c6953514c2d582d436c75737465722d312e362e302e372d32303231303630382d6c6f67002a681600396c56683e3a316d00fff7210200ff8115000000000000000000000c604a0372537f6c2565401a006d7973716c5f6e61746976655f70617373776f726400";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        HandshakePacket read = (HandshakePacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(1024);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

}