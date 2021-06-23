package com.youu.mysql.protocol.net.pkg.res.resultset;

import com.mysql.cj.CharsetMapping;
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
        int index = buffer.readerIndex();
        buffer.skipBytes((int)MySQLBufUtil.readLengthEncodedInteger(buffer));
        buffer.skipBytes((int)MySQLBufUtil.readLengthEncodedInteger(buffer));
        buffer.skipBytes((int)MySQLBufUtil.readLengthEncodedInteger(buffer));
        buffer.skipBytes((int)MySQLBufUtil.readLengthEncodedInteger(buffer));
        buffer.skipBytes((int)MySQLBufUtil.readLengthEncodedInteger(buffer));
        buffer.skipBytes(1);
        this.character = buffer.readShortLE();
        String charset = CharsetMapping.getJavaEncodingForCollationIndex(character);
        buffer.readerIndex(index);
        this.schema = MySQLBufUtil.readLenEncString(buffer, charset);
        this.table = MySQLBufUtil.readLenEncString(buffer, charset);
        this.orgTable = MySQLBufUtil.readLenEncString(buffer, charset);
        this.name = MySQLBufUtil.readLenEncString(buffer, charset);
        this.orgName = MySQLBufUtil.readLenEncString(buffer, charset);
        buffer.readByte();
        buffer.skipBytes(2);//this.character = buffer.readShortLE(); already assigned
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
        String charset = CharsetMapping.getJavaEncodingForCollationIndex(character);
        MySQLBufUtil.writeLenEncString(buffer, CATALOG, charset);
        MySQLBufUtil.writeLenEncString(buffer, schema, charset);
        MySQLBufUtil.writeLenEncString(buffer, table, charset);
        MySQLBufUtil.writeLenEncString(buffer, orgTable, charset);
        MySQLBufUtil.writeLenEncString(buffer, name, charset);
        MySQLBufUtil.writeLenEncString(buffer, orgName, charset);
        buffer.writeByte(NEXT_LENGTH);
        buffer.writeShortLE(character);
        buffer.writeIntLE(columnLength);
        buffer.writeByte(type);
        buffer.writeBytes(flags);
        buffer.writeByte(decimals);
        buffer.writeBytes(FILLER);
        if (packet instanceof ComFieldList) {
            //TODO
        }
    }
}
