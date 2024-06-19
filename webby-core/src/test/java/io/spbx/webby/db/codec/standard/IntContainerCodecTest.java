package io.spbx.webby.db.codec.standard;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import io.spbx.util.testing.ext.HppcBytecodeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.spbx.webby.db.codec.AssertCodec.assertCodec;

public class IntContainerCodecTest {
    @RegisterExtension private static final HppcBytecodeExtension HPPC_ORDER_FIX = new HppcBytecodeExtension();

    @Test
    public void int_array_list() throws Exception {
        assertCodec(IntArrayListCodec.INSTANCE).roundtrip(new IntArrayList());
        assertCodec(IntArrayListCodec.INSTANCE).roundtrip(IntArrayList.from(1));
        assertCodec(IntArrayListCodec.INSTANCE).roundtrip(IntArrayList.from(1, 2, 3));
        assertCodec(IntArrayListCodec.INSTANCE).roundtrip(IntArrayList.from(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void int_hash_set() throws Exception {
        assertCodec(IntHashSetCodec.INSTANCE).roundtrip(new IntHashSet());
        assertCodec(IntHashSetCodec.INSTANCE).roundtrip(IntHashSet.from(1));
        assertCodec(IntHashSetCodec.INSTANCE).roundtrip(IntHashSet.from(1, 2, 3));
        assertCodec(IntHashSetCodec.INSTANCE).roundtrip(IntHashSet.from(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }
}
