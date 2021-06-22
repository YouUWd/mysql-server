package com.youu.common;

import java.util.Arrays;

import com.seaboat.mysql.protocol.util.HexUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/11
 */
@Slf4j
public class CommonTest {
    @Test
    public void testByteValue() {
        int i = 0x41;
        System.out.println((byte)i);
        byte f = 4;
        byte s = 1;
        System.out.println((f << 4) + s);
        System.out.println(Integer.parseInt("4a", 16));
        System.out.println(Byte.parseByte("4a", 16));
        byte[] bytes = HexUtil.hexStr2Bytes(
            "4a0000000a382e302e3232000d0000003d637f08466f182e00ffffff0200ffc71500000000000000000000575d7a36223e376377532d6d006d7973716c5f6e61746976655f70617373776f726400");
        System.out.println(Arrays.toString(bytes));
        System.out.println((byte)217);
    }

    @Test
    public void testByteBufferMarkIndex() {
        ByteBuf buf = Unpooled.buffer(16);
        buf.markWriterIndex();
        buf.writeMediumLE(10);
        show(buf);
        buf.writeByte(1);
        buf.writeByte(2);
        buf.writeByte(3);
        buf.resetWriterIndex();
        show(buf);
        buf.writeMediumLE(11);
        show(buf);
    }

    @Test
    public void testByteBufferMerge() {
        ByteBuf buf1 = Unpooled.buffer(3);
        ByteBuf buf2 = Unpooled.buffer(16);

        buf1.writeMediumLE(20);

        buf2.writeMediumLE(10);
        show(buf2);
        buf2.writeByte(1);
        buf2.writeByte(2);
        buf2.writeByte(3);
        show(buf2);

        ByteBuf buf = Unpooled.wrappedBuffer(buf1, buf2);
        System.out.println(buf);
        System.out.println(buf.readUnsignedMediumLE());
        System.out.println(buf.readUnsignedMediumLE());
        System.out.println(buf);

    }

    @Test
    public void testByteBufferSlice() {

        ByteBuf buf = Unpooled.buffer(16);
        ByteBuf buf1 = buf.slice(0, 3);
        System.out.println(buf);
        System.out.println(buf1);

    }

    void show(ByteBuf buf) {
        log.info("buf={}, data={}", buf, Arrays.toString(buf.array()));
    }
}
