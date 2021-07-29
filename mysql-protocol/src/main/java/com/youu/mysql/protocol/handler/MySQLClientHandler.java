package com.youu.mysql.protocol.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/15
 */
@Slf4j
public class MySQLClientHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext userCtx;
    private ChannelHandlerContext storeCtx;

    // init packet is handshake packet
    private boolean initStore = false;
    private final List<ByteBuf> body = new ArrayList<>(8);
    private final BlockingQueue<ByteBuf> responseQueue = new LinkedBlockingQueue<>(1);

    /**
     * 1、handshake
     * 2、login response
     *
     * @return mysql store response
     */
    public ByteBuf response() {
        //HandShakeResponse without request
        ByteBuf result;
        boolean interrupted = false;
        for (; ; ) {
            try {
                result = responseQueue.take();
                break;
            } catch (InterruptedException ignore) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    public void execute(MySQLPacket packet) {
        storeCtx.writeAndFlush(packet);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        this.storeCtx = ctx;

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (initStore) {
            body.add((ByteBuf)msg);
        } else {
            userCtx.write(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (initStore) {
            Optional<ByteBuf> optional = body.stream().reduce(Unpooled::wrappedBuffer);
            optional.ifPresent(byteBuf -> responseQueue.add(byteBuf));
        } else {
            userCtx.flush();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channelInactive");
    }

    public void setInitStore(boolean initStore) {
        this.initStore = initStore;
    }

    public void setUserCtx(ChannelHandlerContext userCtx) {
        this.userCtx = userCtx;
    }

}
