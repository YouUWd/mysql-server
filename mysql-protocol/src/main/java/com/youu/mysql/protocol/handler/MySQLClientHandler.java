package com.youu.mysql.protocol.handler;

import java.security.DigestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.primitives.Bytes;
import com.mysql.cj.CharsetMapping;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.util.StringUtils;
import com.youu.mysql.protocol.common.ChannelAttributeKey;
import com.youu.mysql.protocol.pkg.MySQLPacket;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.pkg.res.HandshakePacket;
import com.youu.mysql.storage.StorageConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
    private CyclicBarrier barrier;

    // init packet is handshake packet
    private final List<ByteBuf> body = new ArrayList<>(8);
    private final BlockingQueue<ByteBuf> responseQueue = new LinkedBlockingQueue<>(1);

    private AtomicLong reqCount = new AtomicLong();

    public MySQLClientHandler(ChannelHandlerContext userCtx, CyclicBarrier barrier) {
        this.userCtx = userCtx;
        this.barrier = barrier;
    }

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

    public void login() throws DigestException {
        ByteBuf handshakeData = response();
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.read(handshakeData);
        byte[] authPluginDataPart1 = handshakePacket.getAuthPluginDataPart1();
        byte[] authPluginDataPart2 = handshakePacket.getAuthPluginDataPart2();
        byte[] authPluginDataPart2True = new byte[authPluginDataPart2.length - 1];
        System.arraycopy(authPluginDataPart2, 0, authPluginDataPart2True, 0, authPluginDataPart2True.length);
        byte[] seed = Bytes.concat(authPluginDataPart1, authPluginDataPart2True);

        LoginRequest loginRequest = userCtx.channel().attr(ChannelAttributeKey.LOGIN_REQUEST).get();
        byte[] passes;
        if ("caching_sha2_password".equals(handshakePacket.getAuthPluginName())) {
            passes = Security
                .scrambleCachingSha2(
                    StringUtils.getBytes(StorageConfig.getConfig().getUserPass().get(loginRequest.getUsername()),
                        CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet())),
                    seed);
            loginRequest.setAuthPluginName("caching_sha2_password");
        } else {
            passes = Security.scramble411(StorageConfig.getConfig().getUserPass().get(loginRequest.getUsername()),
                seed,
                CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet()));
        }

        loginRequest.setAuthResponse(passes);

        storeCtx.writeAndFlush(loginRequest);
        ByteBuf loginResponse = response();
        log.info("loginResponse {}", ByteBufUtil.hexDump(loginResponse));
    }

    public void execute(MySQLPacket packet) {
        log.info("execute {}", packet);
        storeCtx.writeAndFlush(packet);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        this.storeCtx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (userCtx.channel().attr(ChannelAttributeKey.STORE_INDEX).get() != null && reqCount.get() < 2) {
            body.add((ByteBuf)msg);
        } else {
            userCtx.write(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws BrokenBarrierException, InterruptedException {
        long count = reqCount.getAndIncrement();
        if (userCtx.channel().attr(ChannelAttributeKey.STORE_INDEX).get() != null && count < 2) {
            Optional<ByteBuf> optional = body.stream().reduce(Unpooled::wrappedBuffer);
            optional.ifPresent(byteBuf -> responseQueue.add(byteBuf));
        } else {
            userCtx.flush();
            barrier.await();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channelInactive");
    }
}
