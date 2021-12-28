package io.webby.testing;

import com.carrotsearch.hppc.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.fail;

public class TestingPrimitives {
    public static @NotNull IntArrayList newIntArrayList(int... values) {
        return newIntContainer(IntArrayList::new, values);
    }

    public static @NotNull IntHashSet newIntHashSet(int... values) {
        return newIntContainer(IntHashSet::new, values);
    }

    public static <C extends IntContainer> @NotNull C newIntContainer(@NotNull Supplier<C> creator, int... values) {
        C container = creator.get();
        for (int value : values) {
            if (container instanceof IntSet set) {
                set.add(value);
            } else if (container instanceof IntIndexedContainer indexed) {
                indexed.add(value);
            } else if (container instanceof IntDeque deque) {
                deque.addLast(value);
            } else {
                fail("Unsupported int container: " + container);
            }
        }
        return container;
    }

    public static @NotNull LongArrayList newLongArrayList(long... values) {
        return newLongContainer(LongArrayList::new, values);
    }

    public static @NotNull LongHashSet newLongHashSet(long... values) {
        return newLongContainer(LongHashSet::new, values);
    }

    public static <C extends LongContainer> @NotNull C newLongContainer(@NotNull Supplier<C> creator, long... values) {
        C container = creator.get();
        for (long value : values) {
            if (container instanceof LongSet set) {
                set.add(value);
            } else if (container instanceof LongIndexedContainer indexed) {
                indexed.add(value);
            } else if (container instanceof LongDeque deque) {
                deque.addLast(value);
            } else {
                fail("Unsupported long container: " + container);
            }
        }
        return container;
    }
}
