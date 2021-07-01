package com.youu.mysql.protocol.pkg.res;

import java.util.Arrays;

import com.youu.mysql.protocol.pkg.res.resultset.ColumnCountPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Test;

public class ColumnCountPacketTest extends TestCase {
    ColumnCountPacket packet = new ColumnCountPacket();

    @Test
    public void test() {
        ByteBuf buf = Unpooled.buffer(128);
        packet.setSequenceId((byte)1);
        packet.setColumnCount(2);
        packet.write(buf);
        System.out.println(packet.read(buf));
    }

    @Test
    public void testRead() {
        System.out.println(Arrays.toString(ByteBufUtil.decodeHexDump("0100000102")));
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("0100000102"));
        System.out.println(packet.read(buf));
    }

}