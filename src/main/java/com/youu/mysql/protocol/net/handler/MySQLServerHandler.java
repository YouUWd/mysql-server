package com.youu.mysql.protocol.net.handler;

import com.youu.mysql.protocol.net.constant.MySQLColumnType;
import com.youu.mysql.protocol.net.pkg.req.ComQuery;
import com.youu.mysql.protocol.net.pkg.req.ComQuit;
import com.youu.mysql.protocol.net.pkg.req.LoginRequest;
import com.youu.mysql.protocol.net.pkg.res.OkPacket;
import com.youu.mysql.protocol.net.pkg.res.ResultSetPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/11
 */
@Slf4j
@Sharable
public class MySQLServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelActive");
        //https://dev.mysql.com/doc/internals/en/connection-phase-packets.html
        ByteBuf buf = Unpooled.buffer(1024);
        buf.writeIntLE(0x4a);
        buf.writeByte(0x0a);
        buf.writeBytes("8.0.22".getBytes());
        buf.writeByte(0);
        buf.writeIntLE(18);

        byte[] s1 = {0x3b, 0x26, 0x28, 0x6e, 0x25, 0x5e, 0x2f, 0x60};
        buf.writeBytes(s1);
        buf.writeByte(0);
        //capability flags
        buf.writeByte(0xff);
        buf.writeByte(0xff);
        //character set
        buf.writeByte(0xff);
        //status flags
        buf.writeByte(0x02);
        buf.writeByte(0);
        //capability flags
        buf.writeByte(0xff);
        buf.writeByte(0xc7);
        //length of auth-plugin-data
        buf.writeByte(0x15);
        for (int i = 0; i < 10; i++) {
            buf.writeByte(0);
        }
        byte[] s2 = {0x74, 0x61, 0x09, 0x03, 0x09, 0x01, 0x1c, 0x1c, 0x4e, 0x65, 0x13, 0x70, 0x00};
        buf.writeBytes(s2);
        buf.writeBytes("mysql_native_password".getBytes());
        buf.writeByte(0);

        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("channelRead {}", msg);
        ByteBuf buf = Unpooled.buffer(1024);
        if (msg instanceof LoginRequest) {
            //Response OK for login
            OkPacket ok = OkPacket.builder().build();
            ok.setSequenceId((byte)(((LoginRequest)msg).getSequenceId() + 1));
            ctx.writeAndFlush(ok);
        } else if (msg instanceof ComQuery) {
            //Response for query
            if (((ComQuery)msg).getQuery().contains("@@version_comment")) {
                ResultSetPacket versionPacket = new ResultSetPacket();
                versionPacket.addColumnDefinition("", "", "", "@@version_comment", "", 33, 57,
                    MySQLColumnType.MYSQL_TYPE_VAR_STRING);
                versionPacket.addEofDef();
                versionPacket.addResultSetRow("Source distribution(YouU Ltd.)");
                versionPacket.addEofRow();
                ctx.writeAndFlush(versionPacket);
            } else {
                ResultSetPacket resultSetPacket = new ResultSetPacket();
                resultSetPacket.setSequenceId(((ComQuery)msg).getSequenceId());
                resultSetPacket.addColumnDefinition("d1", "t1", "t1", "id", "id", 63, 11,
                    MySQLColumnType.MYSQL_TYPE_LONG);
                resultSetPacket.addColumnDefinition("d1", "t1", "t1", "name", "name", 33, 48,
                    MySQLColumnType.MYSQL_TYPE_VAR_STRING);
                resultSetPacket.addEofDef();
                resultSetPacket.addResultSetRow("1", "a");
                resultSetPacket.addEofRow();
                ctx.writeAndFlush(resultSetPacket);
            }
        } else if (msg instanceof ComQuit) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
