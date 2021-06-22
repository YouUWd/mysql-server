package com.youu.mysql.protocol.net.pkg.res.resultset;

import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import com.youu.mysql.protocol.net.util.MySQLBufUtil;
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
public class ColumnCountPacket extends MySQLPacket {
    public ColumnCountPacket() {
    }

    public ColumnCountPacket(byte seq) {
        setSequenceId(seq);
    }

    private long columnCount;

    @Override
    protected void readPayload(ByteBuf buffer) {
        this.columnCount = MySQLBufUtil.readLengthEncodedInteger(buffer);
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        MySQLBufUtil.writeLengthEncodedInt(buffer, columnCount);
    }

    public void addColumn() {
        columnCount++;
    }
}
