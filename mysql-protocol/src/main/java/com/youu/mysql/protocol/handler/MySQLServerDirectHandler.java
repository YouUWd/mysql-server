package com.youu.mysql.protocol.handler;

import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import com.youu.mysql.protocol.codec.MySQLEncoder;
import com.youu.mysql.protocol.common.ChannelAttributeKey;
import com.youu.mysql.protocol.pkg.MySQLPacket;
import com.youu.mysql.protocol.pkg.req.ComQuery;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.pkg.res.ErrorPacket;
import com.youu.mysql.protocol.util.MySQLHintUtil;
import com.youu.mysql.storage.StorageConfig;
import com.youu.mysql.storage.StorageConfig.HostPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/11
 */
@Slf4j
@Sharable
public class MySQLServerDirectHandler extends SimpleChannelInboundHandler<MySQLPacket> {

    private static final EventLoopGroup GROUP = new NioEventLoopGroup();
    private CyclicBarrier barrier = new CyclicBarrier(2);

    private Bootstrap bootstrap;

    private MySQLClientHandler schemaHandler;

    private KeyedObjectPool<Integer, MySQLClientHandler> handlerPool;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        bootstrap = new Bootstrap().group(GROUP)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) {
                             ChannelPipeline pipeline = ch.pipeline();
                             // and then business logic.
                             pipeline.addLast(new MySQLEncoder());
                             pipeline.addLast(new MySQLClientHandler(ctx, barrier));
                         }
                     }
            );
        handlerPool = new GenericKeyedObjectPool(new MySQLClientHandlerFactory(bootstrap, barrier));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException, BrokenBarrierException {
        HostPort schemaStore = StorageConfig.getConfig().getSchema();
        log.info("{} {} channelActive {}", schemaStore, GROUP, bootstrap);
        // Make a new connection.
        ChannelFuture f = bootstrap.connect(schemaStore.getHost(), schemaStore.getPort()).sync();
        schemaHandler = (MySQLClientHandler)f.channel().pipeline().last();
        barrier.await();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MySQLPacket packet) throws Exception {
        log.info("{} channelRead {}", GROUP, packet);
        ctx.channel().attr(ChannelAttributeKey.STORE_INDEX).set(null);
        if (packet instanceof LoginRequest) {
            schemaHandler.execute(packet);
            ctx.channel().attr(ChannelAttributeKey.LOGIN_REQUEST).set((LoginRequest)packet);
        } else {
            if (packet instanceof ComQuery) {
                String query = ((ComQuery)packet).getQuery();
                Optional<Integer> optional = MySQLHintUtil.getIndex(query);
                if (optional.isPresent()) {
                    int storeIndex = optional.get();
                    int size = StorageConfig.getConfig().getStorages().size();
                    if (storeIndex < 0 || storeIndex > size) {
                        ErrorPacket error = new ErrorPacket();
                        error.generate("HINT of USE_STORE should between [1," + size + "]");
                        ByteBuf buffer = Unpooled.buffer(128);
                        error.write(buffer);
                        ctx.writeAndFlush(buffer);
                        return;
                    } else {
                        ctx.channel().attr(ChannelAttributeKey.STORE_INDEX).set(storeIndex);
                        try {
                            MySQLClientHandler handler = handlerPool.borrowObject(storeIndex);
                            log.info("handler{}, {}", storeIndex, handler);
                            handler.execute(packet);
                            handlerPool.returnObject(storeIndex, handler);
                        } catch (Exception e) {
                            log.error("exec in store fail...", e);
                            ErrorPacket error = new ErrorPacket();
                            error.generate(e.getMessage());
                            ByteBuf buffer = Unpooled.buffer(1024);
                            error.write(buffer);
                            ctx.writeAndFlush(buffer);
                            return;
                        }
                    }
                } else {
                    schemaHandler.execute(packet);
                }
            } else {
                schemaHandler.execute(packet);
            }
        }
        barrier.await();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exceptionCaught", cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channelInactive");
        ctx.close();
    }
}
