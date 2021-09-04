package io.webby.db.kv.oak;

import com.yahoo.oak.OakScopedReadBuffer;
import com.yahoo.oak.OakScopedWriteBuffer;
import com.yahoo.oak.OakSerializer;
import com.yahoo.oak.OakUnsafeDirectBuffer;
import io.webby.db.codec.Codec;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class OakVariableSizeSerializerAdapter<T> implements OakSerializer<T> {
    private final Codec<T> codec;

    public OakVariableSizeSerializerAdapter(@NotNull Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public void serialize(T object, OakScopedWriteBuffer targetBuffer) {
        OakUnsafeDirectBuffer unsafeTarget = (OakUnsafeDirectBuffer) targetBuffer;
        byte[] bytes = codec.writeToBytes(object);
        unsafeTarget.getByteBuffer().put(unsafeTarget.getOffset(), bytes);
    }

    @Override
    public T deserialize(OakScopedReadBuffer byteBuffer) {
        OakUnsafeDirectBuffer unsafeBuffer = (OakUnsafeDirectBuffer) byteBuffer;
        ByteBuffer unsafeSlice = unsafeBuffer.getByteBuffer().slice(unsafeBuffer.getOffset(), unsafeBuffer.getLength());
        return codec.readFromByteBuffer(unsafeSlice);
    }

    @Override
    public int calculateSize(T object) {
        return codec.writeToBytes(object).length;
    }
}
