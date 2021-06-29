package com.youu.mysql.protocol.net.pkg.res;

import java.util.List;

import com.google.common.collect.Lists;
import com.youu.mysql.common.constant.MySQLColumnType;
import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import com.youu.mysql.protocol.net.pkg.res.resultset.ColumnCountPacket;
import com.youu.mysql.protocol.net.pkg.res.resultset.ColumnDefinitionPacket;
import com.youu.mysql.protocol.net.pkg.res.resultset.ResultSetRowPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/22
 */
@Data
public class ResultSetPacket extends MySQLPacket {
    private byte seq = 0;
    private ColumnCountPacket columnCountPacket = new ColumnCountPacket(increaseAndGetSeq());

    private List<ColumnDefinitionPacket> columnDefinitionPackets = Lists.newArrayList();
    private EofPacket eofDef = new EofPacket();
    private List<ResultSetRowPacket> resultSetRowPackets = Lists.newArrayList();
    private EofPacket eofRow = new EofPacket();

    public void addColumnDefinition(String schema, String tableName, String columnName,
                                    int character, int columnLength, MySQLColumnType columnType) {
        addColumnDefinition(schema, tableName, tableName, columnName, columnName, character, columnLength, columnType);
    }

    public void addColumnDefinition(String schema, String tableName, String orgTableName, String columnName,
                                    String orgColumnName, int character, int columnLength, MySQLColumnType columnType) {
        columnCountPacket.addColumn();
        ColumnDefinitionPacket definitionPacket = ColumnDefinitionPacket.builder()
            .schema(schema)
            .table(tableName)
            .orgTable(orgTableName)
            .name(columnName)
            .orgName(orgColumnName)
            .character(character)
            .columnLength(columnLength)
            .type(columnType.getValue())
            .flags(new byte[] {0, 0})
            .decimals(0x1f)
            .build();
        definitionPacket.setSequenceId(increaseAndGetSeq());
        columnDefinitionPackets.add(definitionPacket);
    }

    public void addEofDef() {
        eofDef.setSequenceId(increaseAndGetSeq());
        eofDef.setStatusFlags(0x0002);
    }

    public void addResultSetRow(String... values) {
        ResultSetRowPacket rowPacket = new ResultSetRowPacket(columnCountPacket);
        rowPacket.setSequenceId(increaseAndGetSeq());
        rowPacket.setValues(Lists.newArrayList(values));
        resultSetRowPackets.add(rowPacket);
    }

    public void addEofRow() {
        eofRow.setSequenceId(increaseAndGetSeq());
        eofRow.setStatusFlags(0x0002);
    }

    @Override
    public MySQLPacket read(ByteBuf buffer) {
        readPayload(buffer);
        return this;
    }

    @Override
    protected void readPayload(ByteBuf buffer) {
        this.columnCountPacket = new ColumnCountPacket();
        this.columnDefinitionPackets.clear();
        this.eofDef = new EofPacket();
        this.resultSetRowPackets.clear();
        this.eofRow = new EofPacket();

        this.columnCountPacket = (ColumnCountPacket)columnCountPacket.read(buffer);
        for (int i = 0; i < columnCountPacket.getColumnCount(); i++) {
            ColumnDefinitionPacket definitionPacket = new ColumnDefinitionPacket();
            this.columnDefinitionPackets.add((ColumnDefinitionPacket)definitionPacket.read(buffer));
        }
        this.eofDef = (EofPacket)eofDef.read(buffer);

        while (buffer.getUnsignedByte(buffer.readerIndex() + 4) != 0xfe) {
            ResultSetRowPacket resultSetRowPacket = new ResultSetRowPacket(columnCountPacket);
            this.resultSetRowPackets.add((ResultSetRowPacket)resultSetRowPacket.read(buffer));
        }
        this.eofRow = (EofPacket)eofRow.read(buffer);
    }

    @Override
    public void write(ByteBuf buffer) {
        writePayload(buffer);
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        columnCountPacket.write(buffer);
        for (ColumnDefinitionPacket definitionPacket : columnDefinitionPackets) {
            definitionPacket.write(buffer);
        }
        eofDef.write(buffer);
        for (ResultSetRowPacket rowPacket : resultSetRowPackets) {
            rowPacket.write(buffer);
        }
        eofRow.write(buffer);
    }

    private byte increaseAndGetSeq() {
        return (byte)(++seq + getSequenceId());
    }
}
