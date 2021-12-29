package io.webby.db.codec.standard;

import com.carrotsearch.hppc.IntHashSet;
import io.webby.db.codec.Codec;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntContainerCodecTest {
    @Test
    public void int_hash_set() {
        assertCodecRoundtrip(IntHashSetCodec.INSTANCE, new IntHashSet());
        assertCodecRoundtrip(IntHashSetCodec.INSTANCE, IntHashSet.from(1));
        assertCodecRoundtrip(IntHashSetCodec.INSTANCE, IntHashSet.from(1, 2, 3));
        assertCodecRoundtrip(IntHashSetCodec.INSTANCE, IntHashSet.from(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    private static <T> void assertCodecRoundtrip(@NotNull Codec<T> codec, @NotNull T value) {
        byte[] bytes = codec.writeToBytes(value);
        T readValue = codec.readFromBytes(bytes);
        assertEquals(bytes.length, codec.sizeOf(value));
        assertEquals(value, readValue);
    }
}
