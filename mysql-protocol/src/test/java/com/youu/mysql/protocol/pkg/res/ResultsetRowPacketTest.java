package com.youu.mysql.protocol.pkg.res;

import com.google.common.collect.Lists;
import com.mysql.cj.MysqlType;
import com.youu.mysql.protocol.pkg.res.resultset.ColumnCountPacket;
import com.youu.mysql.protocol.pkg.res.resultset.ColumnDefinitionPacket;
import com.youu.mysql.protocol.pkg.res.resultset.ResultSetRowPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ResultsetRowPacketTest extends TestCase {
    ResultSetRowPacket packet;

    {
        ColumnCountPacket countPacket = new ColumnCountPacket();
        countPacket.setColumnCount(2);
        packet = new ResultSetRowPacket(countPacket);
    }

    @Test
    public void testRead1() {
        String hexDump = "0400000501310161";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ResultSetRowPacket read = (ResultSetRowPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead2() {
        String hexDump = "1b00000506e4b8ade59bbd13323032312d30362d32322031303a33373a3133";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ResultSetRowPacket read = (ResultSetRowPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testReadAll() {
        ByteBuf buffer = Unpooled.buffer(256);
        byte sequenceId = 1;
        ColumnCountPacket countPacket = new ColumnCountPacket();
        countPacket.setSequenceId(sequenceId++);
        countPacket.setColumnCount(2);
        countPacket.write(buffer);

        System.out.println(ByteBufUtil.hexDump(buffer));

        ColumnDefinitionPacket columnId = ColumnDefinitionPacket.builder()
            .schema("d1")
            .table("t1")
            .orgTable("t1")
            .name("id")
            .orgName("id")
            .character(63)
            .columnLength(11)
            .type(MysqlType.FIELD_TYPE_VAR_STRING)
            .flags(new byte[] {0, 0})
            .decimals(0)
            .build();
        columnId.setSequenceId(sequenceId++);
        columnId.write(buffer);
        System.out.println(ByteBufUtil.hexDump(buffer));

        ColumnDefinitionPacket columnName = ColumnDefinitionPacket.builder()
            .schema("d1")
            .table("t1")
            .orgTable("t1")
            .name("name")
            .orgName("name")
            .character(33)
            .columnLength(48)
            .type(MysqlType.FIELD_TYPE_VAR_STRING)
            .flags(new byte[] {0, 0})
            .decimals(0)
            .build();
        columnName.setSequenceId(sequenceId++);
        columnName.write(buffer);
        System.out.println(ByteBufUtil.hexDump(buffer));

        EofPacket eof = new EofPacket();
        eof.setSequenceId(sequenceId++);
        eof.setWarnings(0);
        eof.setStatusFlags(0x0022);
        eof.write(buffer);
        System.out.println(ByteBufUtil.hexDump(buffer));

        ResultSetRowPacket rowPacket = new ResultSetRowPacket(countPacket);
        rowPacket.setSequenceId(sequenceId++);
        rowPacket.setValues(Lists.newArrayList("1", "a"));
        rowPacket.write(buffer);
        System.out.println(ByteBufUtil.hexDump(buffer));

        eof.setSequenceId(sequenceId++);
        eof.write(buffer);
        System.out.println(ByteBufUtil.hexDump(buffer));
    }

}