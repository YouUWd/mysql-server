package com.youu.mysql.protocol.pkg.res;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/21
 */
@Data
@ToString(callSuper = true)
public class EofPacket extends MySQLPacket {
    private short header = 0xfe;
    private int warnings;
    private int statusFlags;

    @Override
    protected void readPayload(ByteBuf buffer) {
        this.header = buffer.readUnsignedByte();
        this.warnings = buffer.readUnsignedShortLE();
        buffer.readBytes(statusFlags);
        this.statusFlags = buffer.readUnsignedShortLE();
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(header);
        buffer.writeShortLE(warnings);
        buffer.writeShortLE(statusFlags);
    }
}
