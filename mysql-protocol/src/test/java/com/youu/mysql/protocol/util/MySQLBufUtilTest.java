package com.youu.mysql.protocol.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MySQLBufUtilTest {

    private static ByteBuf buf = Unpooled.buffer(1024);

    @Test
    public void test1_write() {
        buf.writeShortLE(10);
        buf.writeMediumLE(100);
        buf.writeIntLE(1000);
        MySQLBufUtil.writeLenEncInt(buf, null);
        MySQLBufUtil.writeLenEncInt(buf, 0L);
        MySQLBufUtil.writeLenEncInt(buf, (long)0xfb);
        MySQLBufUtil.writeLenEncInt(buf, (long)0xfc);
        MySQLBufUtil.writeLenEncInt(buf, (long)0xfd);
        MySQLBufUtil.writeLenEncInt(buf, (long)0xfe);
        MySQLBufUtil.writeLenEncInt(buf, (long)0xfffff1);
        MySQLBufUtil.writeLenEncInt(buf, (long)0xfffffff);

        MySQLBufUtil.writeLenEncString(buf, "Hello");
        MySQLBufUtil.writeNullTerminatedString(buf, "World");
        buf.writeBytes("End".getBytes());
    }

    @Test
    public void test2_read() {
        Assert.assertEquals(10, MySQLBufUtil.readUB2(buf));
        Assert.assertEquals(100, MySQLBufUtil.readUB3(buf));
        Assert.assertEquals(1000, MySQLBufUtil.readUB4(buf));
        Assert.assertEquals(-1, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals(0, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals(0xfb, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals(0xfc, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals(0xfd, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals(0xfe, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals(0xfffff1, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals(0xfffffff, MySQLBufUtil.readLenEncInteger(buf));
        Assert.assertEquals("Hello", MySQLBufUtil.readLenEncString(buf));
        Assert.assertEquals("World", MySQLBufUtil.readNullTerminatedString(buf));
        Assert.assertEquals("End", MySQLBufUtil.readEofString(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test3_write() {
        buf.clear();
        MySQLBufUtil.writeLenEncInt(buf, (long)-1);
    }

}