package com.youu.mysql.protocol.net.pkg.res;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class EofPacketTest extends TestCase {
    EofPacket packet = new EofPacket();

    @Test
    public void test() {
        ByteBuf buf = Unpooled.buffer(128);
        packet.setSequenceId((byte)1);
        packet.setHeader((byte)0xfe);
        packet.setWarnings(2);
        packet.setStatusFlags(0x0022);
        packet.write(buf);
        System.out.println(packet.read(buf));
    }

    @Test
    public void testRead1() {
        String hexDump = "05000004fe00002200";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        EofPacket read = (EofPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead2() {
        String hexDump = "05000006fe00002200";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        EofPacket read = (EofPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }
}