package io.webby.netty.dispatch;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.webby.app.Settings;
import org.jetbrains.annotations.NotNull;

class NettyConst {
    public final int masterThreads;
    public final int workerThreads;
    public final int maxInitLineLength;
    public final int maxHeaderLength;
    public final int maxChunkLength;
    public final int maxContentLength;

    @Inject
    public NettyConst(@NotNull Settings settings) {
        masterThreads = settings.getIntProperty("netty.master.group.threads", 0);
        workerThreads = settings.getIntProperty("netty.worker.group.threads", 0);
        maxInitLineLength = settings.getIntProperty("netty.max.init.line.length", HttpObjectDecoder.DEFAULT_MAX_INITIAL_LINE_LENGTH);
        maxHeaderLength = settings.getIntProperty("netty.max.header.length", HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE);
        maxChunkLength = settings.getIntProperty("netty.max.chunk.length", HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE);
        maxContentLength = settings.getIntProperty("netty.content.max.length.bytes", 10 << 20);
    }
}
