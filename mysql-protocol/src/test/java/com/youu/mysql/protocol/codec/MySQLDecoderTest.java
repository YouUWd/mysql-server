package com.youu.mysql.protocol.codec;

import java.util.List;

import com.google.common.collect.Lists;
import com.youu.mysql.protocol.pkg.MySQLPacket;
import com.youu.mysql.protocol.pkg.req.ComQuery;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class MySQLDecoderTest {
    MySQLDecoder decoder = new MySQLDecoder();

    @Test
    public void decode1() {
        String hexDump
            =
            "bc00000185a67f0000000001210000000000000000000000000000000000000000000000726f6f740014a5e89d16a0527e1b9352517f2b79e7f1e8afdfa56d7973716c5f6e61746976655f70617373776f7264006b035f6f730a6d61636f7331302e31320c5f636c69656e745f6e616d65086c69626d7973716c045f7069640538323736320f5f636c69656e745f76657273696f6e06352e362e3337095f706c6174666f726d067838365f36340c70726f6772616d5f6e616d65056d7973716c";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        MySQLPacket decode = decoder.decode(buf);
        Assert.assertEquals(LoginRequest.class, decode.getClass());
        buf = Unpooled.buffer(128);
        ComQuery packet = new ComQuery();
        packet.setSequenceId((byte)1);
        packet.setQuery("select '中国'");
        packet.write(buf);
        System.out.println(buf);
        decode = decoder.decode(buf);
        Assert.assertEquals(ComQuery.class, decode.getClass());

    }

    @Test
    public void decode2() {
        List<Object> data = Lists.newArrayList();
        String hexDump
            =
            "bc00000185a67f0000000001210000000000000000000000000000000000000000000000726f6f740014a5e89d16a0527e1b9352517f2b79e7f1e8afdfa56d7973716c5f6e61746976655f70617373776f7264006b035f6f730a6d61636f7331302e31320c5f636c69656e745f6e616d65086c69626d7973716c045f7069640538323736320f5f636c69656e745f76657273696f6e06352e362e3337095f706c6174666f726d067838365f36340c70726f6772616d5f6e616d65056d7973716c";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        decoder.decode(null, buf, data);
        Assert.assertEquals(1, data.size());
        buf = Unpooled.buffer(128);
        ComQuery packet = new ComQuery();
        packet.setSequenceId((byte)1);
        packet.setQuery("select '中国'");
        packet.write(buf);
        System.out.println(buf);
        decoder.decode(null, buf, data);
        Assert.assertEquals(2, data.size());

    }
}