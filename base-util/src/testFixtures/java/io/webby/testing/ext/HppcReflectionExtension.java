package io.webby.testing.ext;

import com.carrotsearch.hppc.HashContainers;
import io.webby.util.base.Unchecked;
import io.webby.util.reflect.EasyMembers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Addresses <a href="https://github.com/carrotsearch/hppc/issues/14">Random iteration</a> HPPC feature in tests.
 * <p>
 * This extension patches a shared {@link HashContainers#ITERATION_SEED} value before each test run.
 * This is relatively cheap and usually enough to stabilize the iteration order.
 */
@SuppressWarnings("JavadocReference")
public class HppcReflectionExtension implements BeforeEachCallback {
    private final AtomicInteger seedRef;
    private final int seedValue;

    public HppcReflectionExtension() {
        this(0);
    }

    public HppcReflectionExtension(int seedValue) {
        this.seedRef = findSeedRef();
        this.seedValue = seedValue;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        seedRef.set(seedValue);
    }

    private static @NotNull AtomicInteger findSeedRef() {
        Field iterationSeedField = EasyMembers.findField(HashContainers.class, "ITERATION_SEED");
        assert iterationSeedField != null : "Failed to find the iteration seed in HashContainers";
        iterationSeedField.setAccessible(true);
        return (AtomicInteger) Unchecked.Suppliers.rethrow(() -> iterationSeedField.get(null)).get();
    }
}
