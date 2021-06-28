package com.youu.mysql.protocol.net.pkg.req;

import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/17
 */
@Data
public class ComProcessKill extends MySQLPacket {
    public static final byte ID = 0x0c;
    public long connectionId;

    @Override
    protected void readPayload(ByteBuf buffer) {
        buffer.skipBytes(1);//skip command id
        this.connectionId = buffer.readUnsignedIntLE();
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(ID);
        buffer.writeIntLE((int)connectionId);
    }

}
