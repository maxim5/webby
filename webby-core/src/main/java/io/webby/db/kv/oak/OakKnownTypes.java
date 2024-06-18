package io.webby.db.kv.oak;

import com.google.common.collect.ImmutableMap;
import com.yahoo.oak.OakComparator;
import com.yahoo.oak.OakScopedReadBuffer;
import com.yahoo.oak.OakScopedWriteBuffer;
import com.yahoo.oak.OakSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.yahoo.oak.common.OakCommonBuildersFactory.*;
import static io.spbx.util.base.EasyCast.castAny;

public class OakKnownTypes {
    private static final Map<Class<?>, OakRecord<?>> map = ImmutableMap.of(
        Integer.class, new OakRecord<>(DEFAULT_INT_COMPARATOR, DEFAULT_INT_SERIALIZER, Integer.MIN_VALUE),
        Long.class, new OakRecord<>(new OakLongComparator(), new OakLongSerializer(), Long.MIN_VALUE),
        String.class, new OakRecord<>(DEFAULT_STRING_COMPARATOR, DEFAULT_STRING_SERIALIZER, "")
    );

    public static <T> @Nullable OakRecord<T> lookupRecord(@NotNull Class<T> klass) {
        return castAny(map.get(klass));
    }

    private static class OakLongComparator implements OakComparator<Long> {
        @Override
        public int compareKeys(Long key1, Long key2) {
            return Long.compare(key1, key2);
        }

        @Override
        public int compareSerializedKeys(OakScopedReadBuffer serializedKey1, OakScopedReadBuffer serializedKey2) {
            return Long.compare(serializedKey1.getLong(0), serializedKey2.getLong(0));
        }

        @Override
        public int compareKeyAndSerializedKey(Long key, OakScopedReadBuffer serializedKey) {
            return Long.compare(key, serializedKey.getLong(0));
        }
    }

    private static class OakLongSerializer implements OakSerializer<Long> {
        @Override
        public void serialize(Long value, OakScopedWriteBuffer targetBuffer) {
            targetBuffer.putLong(0, value);
        }

        @Override
        public Long deserialize(OakScopedReadBuffer serializedValue) {
            return serializedValue.getLong(0);
        }

        @Override
        public int calculateSize(Long value) {
            return Long.BYTES;
        }
    }
}
