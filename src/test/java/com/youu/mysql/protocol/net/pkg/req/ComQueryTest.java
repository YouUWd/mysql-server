package com.youu.mysql.protocol.net.pkg.req;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Test;

public class ComQueryTest extends TestCase {
    @Test
    public void test() {
        ByteBuf buf = Unpooled.buffer(128);
        ComQuery packet = new ComQuery();
        packet.setSequenceId((byte)1);
        packet.setQuery("select '中国'");
        packet.write(buf);

        System.out.println(packet.read(buf));
    }
}