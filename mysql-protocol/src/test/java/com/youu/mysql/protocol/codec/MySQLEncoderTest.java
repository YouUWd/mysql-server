package com.youu.mysql.protocol.codec;

import com.youu.mysql.protocol.pkg.req.ComQuery;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class MySQLEncoderTest {
    MySQLEncoder encoder = new MySQLEncoder();

    @Test
    public void encode() {
        ComQuery packet = new ComQuery();
        packet.setSequenceId((byte)1);
        packet.setQuery("select '中国'");
        ByteBuf buf = Unpooled.buffer(128);
        encoder.encode(null, packet, buf);
        Assert.assertEquals("100000010373656c6563742027e4b8ade59bbd27", ByteBufUtil.hexDump(buf));
    }
}