package com.youu.mysql.protocol.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import com.mysql.cj.MysqlType;
import com.youu.mysql.common.util.H2MySQLConverter;
import com.youu.mysql.protocol.common.ConnectionAttr;
import com.youu.mysql.protocol.pkg.req.ComFieldList;
import com.youu.mysql.protocol.pkg.req.ComInitDB;
import com.youu.mysql.protocol.pkg.req.ComProcessKill;
import com.youu.mysql.protocol.pkg.req.ComQuery;
import com.youu.mysql.protocol.pkg.req.ComQuit;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.pkg.res.EofPacket;
import com.youu.mysql.protocol.pkg.res.ErrorPacket;
import com.youu.mysql.protocol.pkg.res.HandshakePacket;
import com.youu.mysql.protocol.pkg.res.OkPacket;
import com.youu.mysql.protocol.pkg.res.ResultSetPacket;
import com.youu.mysql.protocol.pkg.res.resultset.ColumnDefinitionPacket;
import com.youu.mysql.storage.StorageProvider;
import com.youu.mysql.storage.util.ColumnTypeConverter;
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
    private static final AtomicInteger ID = new AtomicInteger(1);
    private static final String SERVER_VERSION = "Source distribution(YouU Ltd.)";
    public static final AttributeKey<ConnectionAttr> CONN_ATTR = AttributeKey.valueOf("conn_attr");

    private StorageProvider storageProvider;

    public MySQLServerHandler(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        int id = ID.getAndIncrement();
        log.info("New ConnectionId:{}, From {}", id, ctx.channel().remoteAddress());
        //https://dev.mysql.com/doc/internals/en/connection-phase-packets.html
        short charset = 255;
        HandshakePacket handshakePacket = HandshakePacket.builder()
            .serverVersion(SERVER_VERSION)
            .connectionId(id)
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
        ConnectionAttr attr = ConnectionAttr.builder().connectionId(id).build();
        ctx.channel().attr(CONN_ATTR).set(attr);
    }

    @SneakyThrows
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ConnectionAttr attr = ctx.channel().attr(CONN_ATTR).get();
        log.info("Accept ConnectionId: {}, {}", attr.getConnectionId(), msg);
        if (msg instanceof LoginRequest) {
            attr.setClientCharset(((LoginRequest)msg).getCharacterSet());
            //Response OK for login
            storageProvider.init(((LoginRequest)msg).getDatabase());
            OkPacket ok = OkPacket.builder().build();
            ok.setSequenceId((byte)(((LoginRequest)msg).getSequenceId() + 1));
            ctx.writeAndFlush(ok);
        } else {
            if (msg instanceof ComInitDB) {
                attr.setSchema(((ComInitDB)msg).getSchema());
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
                        MysqlType.FIELD_TYPE_VAR_STRING);
                    versionPacket.addEofDef();
                    versionPacket.addResultSetRow(SERVER_VERSION);
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
                            int fieldType = ColumnTypeConverter.h22MySQL(metaData.getColumnType(i),
                                metaData.getColumnTypeName(i));
                            //ColumnDefinitionPacket  charset 应当是client的连接的charset，
                            // 也即show variables like 'character_set_results'; 可从LoginRequest获取
                            resultSetPacket.addColumnDefinition(metaData.getSchemaName(i), metaData.getTableName(i),
                                metaData.getTableName(i), metaData.getColumnLabel(i), metaData.getColumnName(i),
                                attr.getClientCharset(), metaData.getColumnDisplaySize(i), fieldType);
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
                        //DDL
                        storageProvider.execute(H2MySQLConverter.convertMySQL(sql));
                        OkPacket ok = OkPacket.builder().build();
                        ok.setSequenceId((byte)(((ComQuery)msg).getSequenceId() + 1));
                        ctx.writeAndFlush(ok);
                    }

                }
            } else if (msg instanceof ComQuit) {
                ctx.close();
            } else if (msg instanceof ComProcessKill) {
                ctx.close();
            } else if (msg instanceof ComFieldList) {
                ColumnDefinitionPacket definitionPacket = ColumnDefinitionPacket.builder()
                    .schema("schema")
                    .table("tableName")
                    .orgTable("orgTableName")
                    .name("columnName")
                    .orgName("orgColumnName")
                    .character(33)
                    .columnLength(1024)
                    .type(MysqlType.FIELD_TYPE_VAR_STRING)
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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ConnectionAttr attr = ctx.channel().attr(CONN_ATTR).get();
        log.error("ExceptionCaught ConnectionId:{}", attr.getConnectionId(), cause);
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
        ConnectionAttr attr = ctx.channel().attr(CONN_ATTR).get();
        log.info("Close ConnectionId:{}", attr.getConnectionId());
        try {
            ctx.channel().attr(CONN_ATTR).set(null);
            storageProvider.release();
        } catch (SQLException exception) {
            log.error("channelInactive ", exception);
        }
    }
}
