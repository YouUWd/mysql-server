package com.youu.mysql.protocol.pkg.req;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import com.youu.mysql.protocol.util.MySQLBufUtil;
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
public class ComFieldList extends MySQLPacket {
    public static final byte ID = 0x04;
    public String table;
    public String fieldWildcard;

    @Override
    protected void readPayload(ByteBuf buffer) {
        buffer.skipBytes(1);//skip command id
        this.table = MySQLBufUtil.readNullTerminatedString(buffer);
        if (buffer.isReadable()) { this.fieldWildcard = MySQLBufUtil.readEofString(buffer); }
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(ID);
        MySQLBufUtil.writeNullTerminatedString(buffer, table);
        if (fieldWildcard != null) {
            buffer.writeBytes(fieldWildcard.getBytes());
        }
    }
}
