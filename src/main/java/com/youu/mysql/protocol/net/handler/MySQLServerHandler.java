package com.youu.mysql.protocol.net.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.mysql.cj.result.Field;
import com.youu.mysql.protocol.net.constant.MySQLColumnType;
import com.youu.mysql.protocol.net.pkg.req.ComFieldList;
import com.youu.mysql.protocol.net.pkg.req.ComInitDB;
import com.youu.mysql.protocol.net.pkg.req.ComProcessKill;
import com.youu.mysql.protocol.net.pkg.req.ComQuery;
import com.youu.mysql.protocol.net.pkg.req.ComQuit;
import com.youu.mysql.protocol.net.pkg.req.LoginRequest;
import com.youu.mysql.protocol.net.pkg.res.EofPacket;
import com.youu.mysql.protocol.net.pkg.res.ErrorPacket;
import com.youu.mysql.protocol.net.pkg.res.HandshakePacket;
import com.youu.mysql.protocol.net.pkg.res.OkPacket;
import com.youu.mysql.protocol.net.pkg.res.ResultSetPacket;
import com.youu.mysql.protocol.net.pkg.res.resultset.ColumnDefinitionPacket;
import com.youu.mysql.protocol.net.storage.H2StorageProvider;
import com.youu.mysql.protocol.net.storage.StorageProvider;
import com.youu.mysql.protocol.net.util.ColumnTypeConverter;
import com.youu.mysql.protocol.net.util.ConnectionId;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
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
    public static final AttributeKey<Short> CLIENT_CHARSET = AttributeKey.valueOf("client-charset");

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
            ctx.channel().attr(CLIENT_CHARSET).set(((LoginRequest)msg).getCharacterSet());
            //Response OK for login
            storageProvider.init(((LoginRequest)msg).getDatabase());
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
                if (sql.contains("select") || sql.contains("SELECT") || sql.contains("show") || sql.contains
                    ("SHOW")) {
                    ResultSetPacket resultSetPacket = new ResultSetPacket();
                    resultSetPacket.setSequenceId(((ComQuery)msg).getSequenceId());
                    ResultSet resultSet = storageProvider.executeQuery(sql);
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        MySQLColumnType type = MySQLColumnType.MYSQL_TYPE_VAR_STRING;
                        if (metaData instanceof com.mysql.cj.jdbc.result.ResultSetMetaData) {
                            Field field = ((com.mysql.cj.jdbc.result.ResultSetMetaData)metaData).getFields()[i - 1];
                            type = MySQLColumnType.lookup(field.getMysqlTypeId());
                        } else if (storageProvider instanceof H2StorageProvider) {
                            type = ColumnTypeConverter.h22MySQL(metaData.getColumnType(i),
                                metaData.getColumnTypeName(i));
                        }
                        //ColumnDefinitionPacket  charset 应当是client的连接的charset，
                        // 也即show variables like 'character_set_results'; 可从LoginRequest获取
                        resultSetPacket.addColumnDefinition(metaData.getSchemaName(i), metaData.getTableName(i),
                            metaData.getTableName(i), metaData.getColumnLabel(i), metaData.getColumnName(i),
                            ctx.channel().attr(CLIENT_CHARSET).get(), metaData.getColumnDisplaySize(i),
                            type);
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
            channelInactive(ctx);
        } else if (msg instanceof ComProcessKill) {
            channelInactive(ctx);
        } else if (msg instanceof ComFieldList) {
            ColumnDefinitionPacket definitionPacket = ColumnDefinitionPacket.builder()
                .schema("schema")
                .table("tableName")
                .orgTable("orgTableName")
                .name("columnName")
                .orgName("orgColumnName")
                .character(33)
                .columnLength(1024)
                .type(MySQLColumnType.MYSQL_TYPE_VAR_STRING.getValue())
                .flags(new byte[] {0, 0})
                .decimals(0x1f)
                .build();
            definitionPacket.setSequenceId((byte)1);
            ctx.write(definitionPacket);

            EofPacket eofDef = new EofPacket();
            eofDef.setSequenceId((byte)2);

            ctx.writeAndFlush(eofDef);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        log.error("exceptionCaught", cause);
        byte seq = 1;
        ErrorPacket errorPacket = new ErrorPacket();
        errorPacket.setSequenceId(seq);
        errorPacket.setSqlStateMarker("#");
        errorPacket.setErrorMessage(cause.getMessage());
        if (cause instanceof SQLException) {
            errorPacket.setErrorCode(((SQLException)cause).getErrorCode());
            errorPacket.setSqlState(((SQLException)cause).getSQLState());
            if (((SQLException)cause).getErrorCode()
                == 4444) {//login error, seq=2 after HandshakePacket 0, LoginRequest 1
                errorPacket.setSequenceId(++seq);
            }
        } else {
            errorPacket.setErrorCode(1000);
            errorPacket.setSqlState("10S00");
        }
        ctx.writeAndFlush(errorPacket);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            ctx.channel().attr(CLIENT_CHARSET).set(null);
            storageProvider.release();
        } catch (SQLException exception) {
            log.error("channelInactive ", exception);
        }
        ctx.close();
    }
}
