package com.youu.mysql.protocol.handler;

import java.util.Optional;

import com.youu.mysql.protocol.codec.MySQLEncoder;
import com.youu.mysql.protocol.common.StorageProperties;
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
import io.netty.util.AttributeKey;
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

    private final EventLoopGroup group = new NioEventLoopGroup();

    private final Bootstrap bootstrap = new Bootstrap().group(group)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) {
                         ChannelPipeline pipeline = ch.pipeline();
                         // and then business logic.
                         pipeline.addLast(new MySQLEncoder());
                         pipeline.addLast(new MySQLClientHandler());
                     }
                 }
        );
    private final AttributeKey<MySQLClientHandler> schemaHandler = AttributeKey.valueOf("schema_handler");
    private final AttributeKey<LoginRequest> loginRequest = AttributeKey.valueOf("login_request");

    private final KeyedObjectPool<StorageProperties, MySQLClientHandler> handlerPool = new GenericKeyedObjectPool<>(
        new MySQLClientHandlerFactory());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        HostPort schemaStore = StorageConfig.getConfig().getSchema();
        log.info("{} {} channelActive {}", schemaStore, group, bootstrap);
        // Make a new connection.
        ChannelFuture f = bootstrap.connect(schemaStore.getHost(), schemaStore.getPort()).sync();
        MySQLClientHandler handler = (MySQLClientHandler)f.channel().pipeline().last();
        handler.setUserCtx(ctx);
        ctx.channel().attr(schemaHandler).set(handler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MySQLPacket packet) throws Exception {
        log.info("{} channelRead {}", group, packet);
        if (packet instanceof LoginRequest) {
            ctx.channel().attr(schemaHandler).get().execute(packet);
            ctx.channel().attr(loginRequest).set((LoginRequest)packet);
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
                    } else {
                        StorageProperties properties = new StorageProperties(
                            StorageConfig.getConfig().getStore(storeIndex), bootstrap,
                            ctx.channel().attr(loginRequest).get());
                        MySQLClientHandler handler = handlerPool.borrowObject(properties);
                        handler.setUserCtx(ctx);
                        log.info("handler {}", handler);
                        handler.execute(packet);
                        handlerPool.returnObject(properties, handler);
                    }
                } else {
                    MySQLClientHandler handler = ctx.channel().attr(schemaHandler).get();
                    handler.execute(packet);
                }
            } else {
                ctx.channel().attr(schemaHandler).get().execute(packet);
            }
        }
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
