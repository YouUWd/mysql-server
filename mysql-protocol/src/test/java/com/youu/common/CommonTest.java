package com.youu.common;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.youu.mysql.storage.StorageConfig;
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

    @Test
    public void testStoreConfig() {
        System.out.println(StorageConfig.getConfig());
    }

    @Test
    public void testBlockingQueue1() throws InterruptedException {
        BlockingQueue queue = new LinkedBlockingDeque(1);
        queue.put(1);
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(queue.poll());
        }).start();
        queue.put(2);

        System.out.println(queue);
    }

    @Test
    public void testBlockingQueue2() {
        BlockingQueue queue = new LinkedBlockingDeque(1);
        System.out.println(queue.poll());
        //System.out.println(queue.take());
        System.out.println(queue);
    }

    void show(ByteBuf buf) {
        log.info("buf={}, data={}", buf, Arrays.toString(buf.array()));
    }
}
