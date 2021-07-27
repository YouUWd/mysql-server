package com.youu.mysql.protocol.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ComFieldListTest {

    ComFieldList packet = new ComFieldList();

    @Test
    public void read() {
        String hexDump = "0400000004743100";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ComFieldList read = (ComFieldList)packet.read(buf);
        buf = Unpooled.buffer(128);
        read.write(buf);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf));
    }

    @Test
    public void write() {
        ComFieldList packet = new ComFieldList();
        packet.setTable("t1");
        ByteBuf buf = Unpooled.buffer(128);
        packet.write(buf);
        Assert.assertEquals("0400000004743100", ByteBufUtil.hexDump(buf));
    }
}