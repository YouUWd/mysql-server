package com.youu.mysql.protocol.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ComQueryTest {
    ComQuery packet = new ComQuery();

    @Test
    public void write() {
        ByteBuf buf = Unpooled.buffer(128);
        ComQuery packet = new ComQuery();
        packet.setQuery("select '中国'");
        packet.write(buf);

        Assert.assertEquals(packet, packet.read(buf));
    }

    @Test
    public void read() {
        String hexDump = "100000000373656c6563742027e4b8ade59bbd27";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ComQuery read = (ComQuery)packet.read(buf);
        System.out.println(read);
        buf = Unpooled.buffer(128);
        read.write(buf);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf));
    }
}