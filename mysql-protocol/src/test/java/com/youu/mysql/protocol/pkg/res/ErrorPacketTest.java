package com.youu.mysql.protocol.pkg.res;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ErrorPacketTest {

    ErrorPacket packet = new ErrorPacket();

    @Test
    public void testRead1() {
        String hexDump
            = "2b000001ff1e04233432533232556e6b6e6f776e20636f6c756d6e2027612720696e20276669656c64206c69737427";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ErrorPacket read = (ErrorPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }
}