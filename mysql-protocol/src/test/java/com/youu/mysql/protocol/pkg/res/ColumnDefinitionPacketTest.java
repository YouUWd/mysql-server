package com.youu.mysql.protocol.pkg.res;

import com.youu.mysql.protocol.pkg.res.resultset.ColumnDefinitionPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ColumnDefinitionPacketTest extends TestCase {
    ColumnDefinitionPacket packet = new ColumnDefinitionPacket();

    @Test
    public void test() {
        ByteBuf buf = Unpooled.buffer(128);
        packet.setSequenceId((byte)1);
        //packet.setColumnCount(2);
        packet.write(buf);
        System.out.println(packet.read(buf));
    }

    @Test
    public void testRead1() {
        String hexDump = "20000002036465660264310274310274310269640269640c3f000b000000030000000000";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ColumnDefinitionPacket read = (ColumnDefinitionPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead2() {
        String hexDump = "2400000303646566026431027431027431046e616d65046e616d650c210030000000fd0000000000";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ColumnDefinitionPacket read = (ColumnDefinitionPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }
}