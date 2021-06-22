package com.youu.mysql.protocol.net.pkg.res;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Test;

public class OkPacketTest extends TestCase {
    @Test
    public void test() {
        ByteBuf buf = Unpooled.buffer(128);
        OkPacket packet = OkPacket.builder().header((byte)1).affectedRow(1).lastInsertId(2).warnings(3).build();
        packet.setSequenceId((byte)1);

        packet.write(buf);

        System.out.println(packet.read(buf));
    }

}