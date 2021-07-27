package com.youu.mysql.protocol.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ComProcessKillTest {
    ComProcessKill packet = new ComProcessKill();

    @Test
    public void read() {
        String hexDump = "050000000c01000000";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ComProcessKill read = (ComProcessKill)packet.read(buf);
        buf = Unpooled.buffer(128);
        read.write(buf);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf));
    }

    @Test
    public void write() {
        ComProcessKill packet = new ComProcessKill();
        packet.setConnectionId(1);
        ByteBuf buf = Unpooled.buffer(32);
        packet.write(buf);
        Assert.assertEquals("050000000c01000000", ByteBufUtil.hexDump(buf));
    }
}