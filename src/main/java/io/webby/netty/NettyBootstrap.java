package io.webby.netty;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.webby.util.Lifetime;

import java.util.logging.Level;

public class NettyBootstrap {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final Lifetime.Definition rootLifetime = new Lifetime.Definition();

    @Inject private Provider<NettyChannelHandler> nettyChannelHandler;

    public void runLocally(int port) throws InterruptedException {
        attachShutdownHook();

        EventLoopGroup masterGroup = new NioEventLoopGroup();
        rootLifetime.onTerminate(masterGroup::shutdownGracefully);

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        rootLifetime.onTerminate(workerGroup::shutdownGracefully);

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
            rootLifetime.terminate();
        }
    }

    @Provides
    public Lifetime getLifetime() {
        return rootLifetime;
    }

    private void attachShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.at(Level.WARNING).log("Shutdown hook received. Terminating");
            rootLifetime.terminate();
            log.at(Level.WARNING).log("Terminated");
            System.out.println("Terminated");
        }));
    }
}
