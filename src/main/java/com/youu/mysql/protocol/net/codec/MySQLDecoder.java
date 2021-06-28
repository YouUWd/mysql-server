package com.youu.mysql.protocol.net.codec;

import java.util.List;

import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import com.youu.mysql.protocol.net.pkg.req.ComFieldList;
import com.youu.mysql.protocol.net.pkg.req.ComInitDB;
import com.youu.mysql.protocol.net.pkg.req.ComPacket;
import com.youu.mysql.protocol.net.pkg.req.ComProcessKill;
import com.youu.mysql.protocol.net.pkg.req.ComQuery;
import com.youu.mysql.protocol.net.pkg.req.ComQuit;
import com.youu.mysql.protocol.net.pkg.req.LoginRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/16
 */
@Slf4j
public class MySQLDecoder extends ByteToMessageDecoder {
    //client第一次发送的是LoginRequest,后续是Command Phase
    private boolean firstRequest = true;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.isReadable(4)) {
            int packetSize = byteBuf.getUnsignedMediumLE(0);
            if (byteBuf.isReadable(packetSize + 3/*Packet Length*/ + 1/*Packet Num*/)) {

            } else {
                // data is not full
                log.warn("wait data {}", byteBuf);
                return;
            }
        } else {
            log.warn("no data {}", byteBuf);
            return;
        }
        list.add(decode(byteBuf));
    }

    public MySQLPacket decode(ByteBuf buf) {
        if (firstRequest) {
            firstRequest = false;
            LoginRequest request = new LoginRequest();
            request.read(buf);
            return request;
        }

        byte commandId = buf.getByte(4);
        MySQLPacket result;
        switch (commandId) {
            case ComQuit.ID:
                ComQuit quit = new ComQuit();
                quit.read(buf);
                result = quit;
                break;
            case ComInitDB.ID:
                ComInitDB initDB = new ComInitDB();
                initDB.read(buf);
                result = initDB;
                break;
            case ComQuery.ID:
                ComQuery query = new ComQuery();
                query.read(buf);
                result = query;
                break;
            case ComFieldList.ID:
                ComFieldList fieldList = new ComFieldList();
                fieldList.read(buf);
                result = fieldList;
                break;
            case ComProcessKill.ID:
                ComProcessKill kill = new ComProcessKill();
                kill.read(buf);
                result = kill;
                break;
            default:
                ComPacket packet = new ComPacket();
                packet.read(buf);
                result = packet;
                break;
        }
        return result;
    }
}
