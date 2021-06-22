package com.youu.mysql.protocol.net.pkg.req;

import java.nio.charset.Charset;

import com.youu.mysql.protocol.net.pkg.MySQLPacket;
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
    private static final byte ID = -1;
    private String payload;

    @Override
    protected void readPayload(ByteBuf buffer) {
        buffer.skipBytes(1);//skip command id
        byte[] pb = new byte[getPayloadLength() - 1];
        buffer.readBytes(pb);
        this.payload = new String(pb, Charset.defaultCharset());
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(ID);
        byte[] bytes = payload.getBytes(Charset.defaultCharset());
        buffer.writeBytes(bytes);
    }

}
