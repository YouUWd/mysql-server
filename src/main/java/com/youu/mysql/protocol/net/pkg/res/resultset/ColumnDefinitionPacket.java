package com.youu.mysql.protocol.net.pkg.res.resultset;

import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import com.youu.mysql.protocol.net.pkg.req.ComFieldList;
import com.youu.mysql.protocol.net.util.MySQLBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/21
 */
@Data
@Builder
@ToString(callSuper = true, exclude = "packet")
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDefinitionPacket extends MySQLPacket {
    private static final String CATALOG = "def";
    private String schema, table, orgTable, name, orgName;
    private static final byte NEXT_LENGTH = 0x0c;
    private int character;
    private int columnLength;
    private int type;//1 reference of
    private byte[] flags = new byte[2];
    private int decimals;
    private static final byte[] FILLER = {0, 0};
    private long lengthOfDefaultValue;
    private String defaultValue;

    private MySQLPacket packet;

    @Override
    protected void readPayload(ByteBuf buffer) {
        MySQLBufUtil.readLenEncString(buffer);
        this.schema = MySQLBufUtil.readLenEncString(buffer);
        this.table = MySQLBufUtil.readLenEncString(buffer);
        this.orgTable = MySQLBufUtil.readLenEncString(buffer);
        this.name = MySQLBufUtil.readLenEncString(buffer);
        this.orgName = MySQLBufUtil.readLenEncString(buffer);
        buffer.readByte();
        this.character = buffer.readShortLE();
        this.columnLength = buffer.readIntLE();
        this.type = buffer.readUnsignedByte();
        buffer.readBytes(flags);
        this.decimals = buffer.readByte();
        buffer.readBytes(FILLER);
        if (packet instanceof ComFieldList) {
            this.lengthOfDefaultValue = MySQLBufUtil.readLengthEncodedInteger(buffer);
        }
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        MySQLBufUtil.writeLenEncString(buffer, CATALOG);
        MySQLBufUtil.writeLenEncString(buffer, this.schema);
        MySQLBufUtil.writeLenEncString(buffer, this.table);
        MySQLBufUtil.writeLenEncString(buffer, this.orgTable);
        MySQLBufUtil.writeLenEncString(buffer, this.name);
        MySQLBufUtil.writeLenEncString(buffer, this.orgName);
        buffer.writeByte(NEXT_LENGTH);
        buffer.writeShortLE(this.character);
        buffer.writeIntLE(this.columnLength);
        buffer.writeByte(this.type);
        buffer.writeBytes(this.flags);
        buffer.writeByte(this.decimals);
        buffer.writeBytes(FILLER);
        if (packet instanceof ComFieldList) {
            //TODO
        }
    }
}
