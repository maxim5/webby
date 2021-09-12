package io.webby.examples.hello;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Serve;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

@Serve
public class ReturnValue {
    @GET(url = "/r/bytes/{val}")
    public byte[] bytes(String val) {
        return val.getBytes(Charset.defaultCharset());
    }

    @GET(url = "/r/byteBuf/{val}")
    public ByteBuf byteBuf(String val) {
        return Unpooled.copiedBuffer(val, Charset.defaultCharset());
    }

    @GET(url = "/r/byteBuffer/{val}")
    public ByteBuffer byteBuffer(String val) {
        return ByteBuffer.wrap(bytes(val));
    }

    @GET(url = "/r/stream/{val}")
    public ByteArrayInputStream stream(String val) {
        return new ByteArrayInputStream(bytes(val));
    }

    @GET(url = "/r/byteChannel/{val}")
    public ReadableByteChannel channel(String val) {
        return Channels.newChannel(stream(val));
    }

    @GET(url = "/r/chars/{val}")
    public char[] chars(String val) {
        return val.toCharArray();
    }

    @GET(url = "/r/charBuffer/{val}")
    public CharBuffer charBuffer(String val) {
        return CharBuffer.wrap(val);
    }

    @GET(url = "/r/charSeq/{val}")
    public CharSequence char_sequence(String val) {
        return new StringBuilder(val);
    }

    @GET(url = "/r/reader/{val}")
    public Reader reader(String val) {
        return new StringReader(val);
    }

    @GET(url = "/r/file/{path}")
    public File file(String path) {
        return new File(path);
    }
}
