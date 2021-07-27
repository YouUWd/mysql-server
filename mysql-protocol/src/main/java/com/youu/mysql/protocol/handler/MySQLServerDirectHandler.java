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

    private final HostPort schemaStore = StorageConfig.getConfig().getSchema();

    private final KeyedObjectPool<StorageProperties, MySQLClientHandler> handlerPool = new GenericKeyedObjectPool<>(
        new MySQLClientHandlerFactory());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        log.info("{} channelActive {}", group, bootstrap);
        // Make a new connection.
        ChannelFuture f = bootstrap.connect(schemaStore.getHost(), schemaStore.getPort()).sync();
        // Get the handler instance to retrieve the answer.
        MySQLClientHandler handler = (MySQLClientHandler)f.channel().pipeline().last();
        ctx.channel().attr(schemaHandler).set(handler);
        ByteBuf handshake = handler.handshake();
        log.info("handshake {}", handshake);
        ctx.writeAndFlush(handshake);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MySQLPacket packet) throws Exception {
        log.info("{} channelRead {}", group, packet);
        ByteBuf response;
        if (packet instanceof LoginRequest) {
            response = ctx.channel().attr(schemaHandler).get().execute(packet);
            //OK
            if (response.getUnsignedByte(4) == 0x00 || response.getUnsignedByte(4) == 0xfe) {
                ctx.channel().attr(loginRequest).set((LoginRequest)packet);
            }
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
                        response = buffer;
                    } else {
                        StorageProperties properties = new StorageProperties(
                            StorageConfig.getConfig().getStore(storeIndex), bootstrap,
                            ctx.channel().attr(loginRequest).get());
                        MySQLClientHandler handler = handlerPool.borrowObject(properties);
                        log.info("handler {}", handler);
                        response = handler.execute(packet);
                        handlerPool.returnObject(properties, handler);
                    }
                } else {
                    response = ctx.channel().attr(schemaHandler).get().execute(packet);
                }
            } else {
                response = ctx.channel().attr(schemaHandler).get().execute(packet);
            }
        }
        ctx.writeAndFlush(response);

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
