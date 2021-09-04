package io.webby.db.kv.oak;

import com.yahoo.oak.OakScopedReadBuffer;
import com.yahoo.oak.OakScopedWriteBuffer;
import com.yahoo.oak.OakSerializer;
import com.yahoo.oak.common.intbuffer.OakIntBufferSerializer;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecSize;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class OakFixedSizeSerializerAdapter<T> implements OakSerializer<T> {
    private final Codec<T> codec;
    private final int sizeInBytes;
    private final OakIntBufferSerializer bufferSerializer;

    public OakFixedSizeSerializerAdapter(@NotNull Codec<T> codec) {
        assert codec.size().isFixed() : "Only fixed size codecs are supported: %s".formatted(codec);

        this.codec = codec;
        this.sizeInBytes = (int) codec.size().numBytes();
        this.bufferSerializer = new OakIntBufferSerializer((sizeInBytes + Integer.BYTES - 1) / Integer.BYTES);
    }

    @Override
    public void serialize(T object, OakScopedWriteBuffer targetBuffer) {
        ByteBuffer buffer = codec.writeToByteBuffer(object);
        bufferSerializer.serialize(buffer, targetBuffer);
    }

    @Override
    public T deserialize(OakScopedReadBuffer byteBuffer) {
        ByteBuffer buffer = bufferSerializer.deserialize(byteBuffer);
        return codec.readFromByteBuffer(buffer);
    }

    @Override
    public int calculateSize(T object) {
        return sizeInBytes;
    }
}
