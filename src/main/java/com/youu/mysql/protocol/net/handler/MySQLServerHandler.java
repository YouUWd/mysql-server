package com.youu.mysql.protocol.net.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.youu.mysql.protocol.net.constant.MySQLColumnType;
import com.youu.mysql.protocol.net.pkg.req.ComInitDB;
import com.youu.mysql.protocol.net.pkg.req.ComQuery;
import com.youu.mysql.protocol.net.pkg.req.ComQuit;
import com.youu.mysql.protocol.net.pkg.req.LoginRequest;
import com.youu.mysql.protocol.net.pkg.res.HandshakePacket;
import com.youu.mysql.protocol.net.pkg.res.OkPacket;
import com.youu.mysql.protocol.net.pkg.res.ResultSetPacket;
import com.youu.mysql.protocol.net.storage.StorageProvider;
import com.youu.mysql.protocol.net.util.ConnectionId;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/11
 */
@Slf4j
@Sharable
public class MySQLServerHandler extends ChannelInboundHandlerAdapter {
    private StorageProvider storageProvider;

    public MySQLServerHandler(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channelActive {}", ConnectionId.get());
        //https://dev.mysql.com/doc/internals/en/connection-phase-packets.html
        short charset = 255;
        HandshakePacket handshakePacket = HandshakePacket.builder()
            .serverVersion("8.0.22-HTAP")
            .connectionId(ConnectionId.get())
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

    @SneakyThrows
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("channelRead {} {}", msg, ConnectionId.get());
        if (msg instanceof LoginRequest) {
            //Response OK for login
            storageProvider.init("");
            OkPacket ok = OkPacket.builder().build();
            ok.setSequenceId((byte)(((LoginRequest)msg).getSequenceId() + 1));
            ctx.writeAndFlush(ok);
        } else if (msg instanceof ComInitDB) {
            storageProvider.init(((ComInitDB)msg).getSchema());
            OkPacket ok = OkPacket.builder().build();
            ok.setSequenceId((byte)(((ComInitDB)msg).getSequenceId() + 1));
            ctx.writeAndFlush(ok);
        } else if (msg instanceof ComQuery) {
            //Response for query
            String sql = ((ComQuery)msg).getQuery();
            if (sql.contains("@@version_comment")) {
                ResultSetPacket versionPacket = new ResultSetPacket();
                versionPacket.addColumnDefinition("", "", "", "@@version_comment", "", 33, 57,
                    MySQLColumnType.MYSQL_TYPE_VAR_STRING);
                versionPacket.addEofDef();
                versionPacket.addResultSetRow("Source distribution(YouU Ltd.)");
                versionPacket.addEofRow();
                ctx.writeAndFlush(versionPacket);
            } else {
                if (sql.contains("select") || sql.contains("SELECT") || sql.contains("show") || sql.contains("SHOW")) {
                    ResultSetPacket resultSetPacket = new ResultSetPacket();
                    resultSetPacket.setSequenceId(((ComQuery)msg).getSequenceId());
                    ResultSet resultSet = storageProvider.executeQuery(sql);
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        resultSetPacket.addColumnDefinition(metaData.getSchemaName(i), metaData.getTableName(i),
                            metaData.getTableName(i), metaData.getColumnLabel(i), metaData.getColumnName(i),
                            33, metaData.getColumnDisplaySize(i), MySQLColumnType.MYSQL_TYPE_VAR_STRING);
                    }
                    resultSetPacket.addEofDef();
                    while (resultSet.next()) {
                        String[] data = new String[columnCount];
                        for (int i = 0; i < columnCount; i++) {
                            data[i] = resultSet.getString(i + 1);
                        }
                        resultSetPacket.addResultSetRow(data);
                    }
                    resultSetPacket.addEofRow();
                    ctx.writeAndFlush(resultSetPacket);
                } else {
                    storageProvider.execute(sql);
                    OkPacket ok = OkPacket.builder().build();
                    ok.setSequenceId((byte)(((ComQuery)msg).getSequenceId() + 1));
                    ctx.writeAndFlush(ok);
                }

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
