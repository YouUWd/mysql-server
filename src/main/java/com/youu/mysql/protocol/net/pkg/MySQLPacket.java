package com.youu.mysql.protocol.net.pkg;

import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @Author Timmy
 * @Description focus that
 * 1、Sending More Than 16Mbyte
 * 2、Sequence ID
 * @Date 2021/6/16
 */
@Data
public abstract class MySQLPacket {
    private int payloadLength;
    private byte sequenceId;

    public MySQLPacket read(ByteBuf buffer) {
        this.payloadLength = buffer.readUnsignedMediumLE();
        this.sequenceId = buffer.readByte();
        readPayload(buffer);
        return this;
    }

    public void write(ByteBuf buffer) {
        int writerIdx = buffer.writerIndex();
        buffer.writeMediumLE(0);
        buffer.writeByte(sequenceId);
        writePayload(buffer);
        int len = buffer.writerIndex() - writerIdx - 4;
        buffer.setMediumLE(writerIdx, len);
    }

    protected abstract void readPayload(ByteBuf buffer);

    protected abstract void writePayload(ByteBuf buffer);
}
