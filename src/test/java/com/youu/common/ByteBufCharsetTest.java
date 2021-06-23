package com.youu.common;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/22
 */
public class ByteBufCharsetTest {
    @Test
    public void testGBK() throws UnsupportedEncodingException {
        String s = "中国";
        byte[] bytes = ByteBufUtil.decodeHexDump("e4b83f9bbd");
        String x = new String(bytes, "GBK");
        System.out.println(x);
        ByteBuf buf = Unpooled.buffer(128);
        buf.writeBytes(x.getBytes("GBK"));
        System.out.println(ByteBufUtil.hexDump(buf));

    }
}
