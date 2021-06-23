package com.youu.mysql.protocol.net.handler;

import com.youu.mysql.protocol.net.constant.MySQLColumnType;
import com.youu.mysql.protocol.net.pkg.req.ComQuery;
import com.youu.mysql.protocol.net.pkg.req.ComQuit;
import com.youu.mysql.protocol.net.pkg.req.LoginRequest;
import com.youu.mysql.protocol.net.pkg.res.HandshakePacket;
import com.youu.mysql.protocol.net.pkg.res.OkPacket;
import com.youu.mysql.protocol.net.pkg.res.ResultSetPacket;
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
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channelActive");
        //https://dev.mysql.com/doc/internals/en/connection-phase-packets.html
        short charset = 255;
        HandshakePacket handshakePacket = HandshakePacket.builder()
            .serverVersion("8.0.22-HTAP")
            .connectionId(1)
            .authPluginDataPart1(new byte[] {1, 2, 3, 4, 5, 6, 7, 8})
            .capabilityFlags1(0xffff)
            .characterSet(charset)
            .statusFlags(0x0002)
            .capabilityFlags2(0xffc7)
            .authPluginDataLength((short)21)
            .authPluginDataPart2(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13})
            .authPluginName("mysql_native_password")
            .build();
        ctx.writeAndFlush(handshakePacket);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("channelRead {}", msg);
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
