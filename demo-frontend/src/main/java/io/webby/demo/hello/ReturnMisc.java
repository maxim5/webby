package io.webby.demo.hello;

import io.netty.handler.codec.http.HttpRequest;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Serve;
import org.jetbrains.annotations.NotNull;

import java.io.*;

@Serve
public class ReturnMisc {
    @GET(url="/r/void")
    public void misc_void(@NotNull HttpRequest request) {
    }

    @GET(url="/r/null")
    public Object misc_null() {
        return null;
    }

    @GET(url="/r/error/npe")
    public Object error_npe() {
        throw new NullPointerException("Message");
    }

    @GET(url = "/r/error/stream/read")
    public InputStream stream_fails_to_read() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Forced failure");
            }
        };
    }

    @GET(url = "/r/error/stream/close")
    public InputStream stream_fails_to_close() {
        return new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("Forced failure");
            }
        };
    }

    @GET(url = "/r/error/reader/read")
    public Reader reader_fails_to_read() {
        return new Reader() {
            @Override
            public int read(char[] buf, int off, int len) throws IOException {
                throw new IOException("Forced failure");
            }

            @Override
            public void close() {}
        };
    }

    @GET(url = "/r/error/reader/close")
    public Reader reader_fails_to_close() {
        return new Reader() {
            @Override
            public int read(char[] buf, int off, int len) {
                return -1;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("Forced failure");
            }
        };
    }
}
