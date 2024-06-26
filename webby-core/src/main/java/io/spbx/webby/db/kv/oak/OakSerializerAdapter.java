package io.spbx.webby.db.kv.oak;

import com.yahoo.oak.OakScopedReadBuffer;
import com.yahoo.oak.OakScopedWriteBuffer;
import com.yahoo.oak.OakSerializer;
import com.yahoo.oak.OakUnsafeDirectBuffer;
import io.spbx.webby.db.codec.Codec;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class OakSerializerAdapter<T> implements OakSerializer<T> {
    private final Codec<T> codec;
    private final int fixedSizeInBytes;

    public OakSerializerAdapter(@NotNull Codec<T> codec) {
        this.codec = codec;
        this.fixedSizeInBytes = codec.size().isFixed() ? codec.size().numBytes() : -1;
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
        int size;
        return fixedSizeInBytes >= 0 ? fixedSizeInBytes :
                (size = codec.sizeOf(object)) >= 0 ? size :
                        codec.writeToBytes(object).length;
    }
}
