package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.webby.app.AppLifetime;
import io.webby.util.AnyLog;
import io.webby.util.Lifetime;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class NettyBootstrap {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Provider<NettyChannelHandler> nettyChannelHandler;

    private final Lifetime.Definition lifetime;

    @Inject
    public NettyBootstrap(@NotNull AppLifetime appLifetime) {
        lifetime = appLifetime.getLifetime();
    }

    public void runLocally(int port) throws InterruptedException {
        attachShutdownHook();

        EventLoopGroup masterGroup = new NioEventLoopGroup();
        lifetime.onTerminate(masterGroup::shutdownGracefully);

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        lifetime.onTerminate(workerGroup::shutdownGracefully);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                            pipeline.addLast(nettyChannelHandler.get());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture httpChannel = bootstrap.bind(port).sync();
            log.at(Level.INFO).log("Server running at %d", port);
            httpChannel.channel().closeFuture().sync();
        } finally {
            lifetime.terminateIfAlive();    // normally a shutdown hook is called, so it shouldn't do anything
        }
    }

    private void attachShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.at(Level.WARNING).log("Shutdown hook received. Terminating application lifetime");
            lifetime.terminate();
            log.at(Level.WARNING).log("Lifetime terminated");
            AnyLog.shutdown();
        }));
    }
}
