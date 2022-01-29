package io.webby.db.count.impl;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

record TestRate(int key, int val) {
    public TestRate {
        assertTrue(key > 0);
        assertTrue(-1 <= val && val <= 1);
    }

    public static @NotNull TestRate from(int signed) {
        assertTrue(signed != 0);
        return new TestRate(Math.abs(signed), signed > 0 ? 1 : -1);
    }

    public static @NotNull TestRate fromAny(@NotNull Object obj) {
        if (obj instanceof Integer signed) {
            return TestRate.from(signed);
        } else if (obj instanceof TestRate testRate) {
            return testRate;
        }
        throw new IllegalArgumentException("Unrecognized item: " + obj);
    }

    public static @NotNull List<TestRate> rates(@NotNull Object @NotNull ... array) {
        return Arrays.stream(array).map(TestRate::fromAny).toList();
    }

    public static @NotNull TestRate like(int key) {
        return new TestRate(Math.abs(key), 1);
    }

    public static @NotNull TestRate dis(int key) {
        return new TestRate(Math.abs(key), -1);
    }

    public static @NotNull TestRate none(int key) {
        return new TestRate(Math.abs(key), 0);
    }
}
