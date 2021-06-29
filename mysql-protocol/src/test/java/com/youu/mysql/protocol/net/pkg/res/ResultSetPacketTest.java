package com.youu.mysql.protocol.net.pkg.res;

import com.youu.mysql.common.constant.MySQLColumnType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ResultSetPacketTest extends TestCase {
    ResultSetPacket packet = new ResultSetPacket();

    @Test
    public void test() {
        ByteBuf buf = Unpooled.buffer(128);
        packet.addColumnDefinition("", "", "", "@@version_comment", "", 33, 57, MySQLColumnType.MYSQL_TYPE_VAR_STRING);
        packet.addEofDef();
        packet.addResultSetRow("Source distribution");
        packet.addEofRow();
        packet.writePayload(buf);
        Assert.assertEquals(
            "0100000101270000020364656600000011404076657273696f6e5f636f6d6d656e74000c210039000000fd00001f000005000003fe000002001400000413536f7572636520646973747269627574696f6e05000005fe00000200",
            ByteBufUtil.hexDump(buf));
    }

    @Test
    public void test1() {
        ByteBuf buf = Unpooled.buffer(1024);
        packet.setSequenceId((byte)0);
        packet.addColumnDefinition("d1", "t1", "t1", "id", "id", 63, 11,
            MySQLColumnType.MYSQL_TYPE_LONG);
        packet.addColumnDefinition("d1", "t1", "t1", "name", "name", 33, 48,
            MySQLColumnType.MYSQL_TYPE_VAR_STRING);
        packet.addEofDef();
        packet.addResultSetRow("1", "a");
        packet.addEofRow();
        packet.writePayload(buf);
        System.out.println(packet);

    }

    @Test
    public void testRead1() {
        String hexDump
            =
            "010000010220000002036465660264310274310274310269640269640c3f000b0000000300000000002400000303646566026431027431027431046e616d65046e616d650c210030000000fd000000000005000004fe00002200040000050131016105000006fe00002200";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ResultSetPacket read = (ResultSetPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead2() {
        String hexDump
            =
            "010000010220000002036465660264310274310274310269640269640c3f000b0000000300001f00002400000303646566026431027431027431046e616d65046e616d650c210030000000fd00001f000005000004fe00000200040000050131016105000006fe00000200";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ResultSetPacket read = (ResultSetPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead3() {
        String hexDump
            =
            "01000001011b0000020364656600000005e4b83f9bbd000c1c0006000000fd01001f000005000003fe000002000700000406e4b8ade59bbd05000005fe00000200";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ResultSetPacket read = (ResultSetPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

    @Test
    public void testRead4() {
        String hexDump
            =
            "01000001011c0000020364656600000006e4b8ade59bbd000c210006000000fd01001f000005000003fe000002000700000406e4b8ade59bbd05000005fe00000200";
        ByteBuf buf = Unpooled.wrappedBuffer(
            ByteBufUtil.decodeHexDump(hexDump));
        ResultSetPacket read = (ResultSetPacket)packet.read(buf);
        System.out.println(read);
        ByteBuf buf1 = Unpooled.buffer(128);
        read.write(buf1);
        Assert.assertEquals(hexDump, ByteBufUtil.hexDump(buf1));
    }

}