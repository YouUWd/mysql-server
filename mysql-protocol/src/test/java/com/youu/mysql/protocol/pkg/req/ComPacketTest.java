package com.youu.mysql.protocol.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ComPacketTest {

    ComPacket packet = new ComPacket();

    @Test
    public void write() {
        ByteBuf buf = Unpooled.buffer(128);
        ComPacket packet = new ComPacket();
        packet.setId((byte)3);
        packet.setPayload("select '中国'".getBytes());
        packet.write(buf);

        System.out.println(packet);
        Assert.assertEquals(packet, packet.read(buf));
    }

    @Test
    public void read() {
        String hexDump = "100000000373656c6563742027e4b8ade59bbd27";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ComPacket read = (ComPacket)packet.read(buf);
        System.out.println(read);
        buf = Unpooled.buffer(128);
        read.write(buf);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf));
    }
}