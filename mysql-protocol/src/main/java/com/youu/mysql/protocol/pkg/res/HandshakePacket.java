package com.youu.mysql.protocol.pkg.res;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import com.youu.mysql.protocol.util.MySQLBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Timmy
 * @Description HandshakeV10 Send by server after connect create
 * @Date 2021/6/23
 * @see
 * <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeV10">HandshakeV10</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandshakePacket extends MySQLPacket {
    private static final byte PROTOCOL_VERSION = 0x0a;//[0a] protocol version
    private String serverVersion;//string[NUL]
    private long connectionId;//byte[4]
    private byte[] authPluginDataPart1 = new byte[8];//byte[8]
    private static final byte FILLER = 0x00;//[00] filler
    private int capabilityFlags1;//lower 2 bytes

    private short characterSet;
    private int statusFlags;// 2 bytes
    private int capabilityFlags2;// upper 2 bytes
    private short authPluginDataLength;// 1 bytes

    private static final byte[] RESERVED = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};//reserved (all [00])
    private byte[] authPluginDataPart2;//auth-plugin-data-part-2 ($len=MAX(13, authLength - 8))
    private String authPluginName;//string[NUL]

    @Override
    protected void readPayload(ByteBuf buffer) {
        byte protocolVersion = buffer.readByte();
        assert protocolVersion == PROTOCOL_VERSION;
        this.serverVersion = MySQLBufUtil.readNullTerminatedString(buffer);
        this.connectionId = buffer.readUnsignedIntLE();
        buffer.readBytes(authPluginDataPart1);
        byte filler = buffer.readByte();
        assert filler == FILLER;
        this.capabilityFlags1 = buffer.readUnsignedShortLE();
        this.characterSet = buffer.readUnsignedByte();
        this.statusFlags = buffer.readUnsignedShortLE();
        this.capabilityFlags2 = buffer.readUnsignedShort();
        this.authPluginDataLength = buffer.readUnsignedByte();
        buffer.readBytes(RESERVED);
        // Protocol::HandshakeV10 文档描述不符合实际authPluginDataPart后面都多出了一个0x00，不参与密码加密
        byte[] authPluginDataPart2 = new byte[Math.max(13, this.authPluginDataLength - 8)];
        buffer.readBytes(authPluginDataPart2);
        this.authPluginDataPart2 = authPluginDataPart2;
        this.authPluginName = MySQLBufUtil.readNullTerminatedString(buffer);
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeByte(PROTOCOL_VERSION);
        MySQLBufUtil.writeNullTerminatedString(buffer, serverVersion);
        buffer.writeIntLE((int)connectionId);
        buffer.writeBytes(authPluginDataPart1);
        buffer.writeByte(FILLER);
        buffer.writeShortLE(capabilityFlags1);
        buffer.writeByte(characterSet);
        buffer.writeShortLE(statusFlags);
        buffer.writeShort(capabilityFlags2);
        buffer.writeByte(authPluginDataLength);
        buffer.writeBytes(RESERVED);
        buffer.writeBytes(authPluginDataPart2);
        MySQLBufUtil.writeNullTerminatedString(buffer, authPluginName);
    }
}
