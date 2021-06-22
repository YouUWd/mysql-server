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
public class ComQuery extends MySQLPacket {
    private static final byte ID = 0x03;
    private String query;

    @Override
    protected void readPayload(ByteBuf buffer) {
        buffer.skipBytes(1);//skip command id
        byte[] qb = new byte[getPayloadLength() - 1];
        buffer.readBytes(qb);
        this.query = new String(qb, Charset.defaultCharset());
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(ID);
        byte[] bytes = query.getBytes(Charset.defaultCharset());
        buffer.writeBytes(bytes);
    }

}
