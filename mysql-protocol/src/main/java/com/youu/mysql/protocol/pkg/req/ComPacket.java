package com.youu.mysql.protocol.pkg.req;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/17
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComPacket extends MySQLPacket {
    private byte id;
    private byte[] payload;

    @Override
    protected void readPayload(ByteBuf buffer) {
        this.id = buffer.readByte();
        this.payload = new byte[getPayloadLength() - 1];
        buffer.readBytes(this.payload);
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(id);
        buffer.writeBytes(payload);
    }

}
