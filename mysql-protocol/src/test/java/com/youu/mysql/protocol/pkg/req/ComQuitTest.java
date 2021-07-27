package com.youu.mysql.protocol.pkg.req;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ComQuitTest {
    ComQuit packet = new ComQuit();

    @Test
    public void readPayload() {
        String hexDump = "0100000001";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        MySQLPacket p = packet.read(buf);
        Assert.assertEquals(new ComQuit(), p);
    }

    @Test
    public void writePayload() {
        ByteBuf buf = Unpooled.buffer(32);
        packet.write(buf);
        Assert.assertEquals("0100000001", ByteBufUtil.hexDump(buf));
    }
}