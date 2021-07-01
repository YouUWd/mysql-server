package com.youu.mysql.protocol.pkg.res;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import com.youu.mysql.protocol.util.MySQLBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/18
 */
@Data
@ToString(callSuper = true)
public class ErrorPacket extends MySQLPacket {
    private static final short HEADER = 0xff;
    private int errorCode;
    private String sqlStateMarker;
    private String sqlState;

    private String errorMessage;

    @Override
    protected void readPayload(ByteBuf buffer) {
        short header = buffer.readUnsignedByte();
        assert header == HEADER;
        this.errorCode = buffer.readUnsignedShortLE();
        byte[] sqlStateMarker = new byte[1];
        buffer.readBytes(sqlStateMarker);
        this.sqlStateMarker = new String(sqlStateMarker);
        byte[] sqlState = new byte[5];
        buffer.readBytes(sqlState);
        this.sqlState = new String(sqlState);
        this.errorMessage = MySQLBufUtil.readEofString(buffer);
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(HEADER);
        buffer.writeShortLE(errorCode);
        buffer.writeBytes(sqlStateMarker.getBytes());
        buffer.writeBytes(sqlState.getBytes());
        buffer.writeBytes(errorMessage.getBytes());
    }

}
