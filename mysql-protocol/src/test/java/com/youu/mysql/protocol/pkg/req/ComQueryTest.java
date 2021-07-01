package com.youu.mysql.protocol.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Test;

public class ComQueryTest extends TestCase {
    ComQuery packet = new ComQuery();

    @Test
    public void test() {
        ByteBuf buf = Unpooled.buffer(128);
        ComQuery packet = new ComQuery();
        packet.setSequenceId((byte)1);
        packet.setQuery("select '中国'");
        packet.write(buf);

        System.out.println(packet.read(buf));
    }

    @Test
    public void testRead() {
        String hexDump = "100000000373656c6563742027e4b8ade59bbd27";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ComQuery read = (ComQuery)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        //Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }
}