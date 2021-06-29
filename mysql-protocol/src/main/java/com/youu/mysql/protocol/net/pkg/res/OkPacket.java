package com.youu.mysql.protocol.net.pkg.res;

import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import com.youu.mysql.protocol.util.MySQLBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/18
 */
@Data
@Builder
@ToString(callSuper = true)
public class OkPacket extends MySQLPacket {
    private byte header;
    private long affectedRow;
    private long lastInsertId;
    private int statusFlags;
    private int warnings;

    private String info;

    private String sessionStateChanges;

    @Override
    protected void readPayload(ByteBuf buffer) {
        this.header = buffer.readByte();
        this.affectedRow = MySQLBufUtil.readLenEncInteger(buffer);
        this.lastInsertId = MySQLBufUtil.readLenEncInteger(buffer);
        this.statusFlags = buffer.readUnsignedShortLE();
        this.warnings = buffer.readUnsignedShortLE();
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(header);
        MySQLBufUtil.writeLenEncInt(buffer, affectedRow);
        MySQLBufUtil.writeLenEncInt(buffer, lastInsertId);
        buffer.writeShortLE(statusFlags);
        buffer.writeShortLE(warnings);
    }

}
