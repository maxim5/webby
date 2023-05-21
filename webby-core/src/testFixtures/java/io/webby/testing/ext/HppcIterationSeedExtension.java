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
 */
public class HppcIterationSeedExtension implements BeforeEachCallback {
    private final AtomicInteger seedRef;
    private final int seedValue;

    public HppcIterationSeedExtension() {
        this(0);
    }

    public HppcIterationSeedExtension(int seedValue) {
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
