package com.youu.mysql.protocol.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ComInitDBTest {
    ComInitDB packet = new ComInitDB();

    @Test
    public void read() {
        String hexDump = "03000000026431";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ComInitDB read = (ComInitDB)packet.read(buf);
        System.out.println(read);
        buf = Unpooled.buffer(128);
        read.write(buf);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf));
    }

    @Test
    public void write() {
        ComInitDB packet = new ComInitDB();
        packet.setSchema("d1");
        ByteBuf buf = Unpooled.buffer(128);
        packet.write(buf);
        Assert.assertEquals("03000000026431", ByteBufUtil.hexDump(buf));
    }
}