package io.webby.db.count.impl;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

record Vote(int key, int val) {
    public Vote {
        assertTrue(key > 0);
        assertTrue(-1 <= val && val <= 1);
    }

    public static @NotNull Vote from(int signed) {
        assertTrue(signed != 0);
        return new Vote(Math.abs(signed), signed > 0 ? 1 : -1);
    }

    public static @NotNull Vote fromAny(@NotNull Object obj) {
        if (obj instanceof Integer signed) {
            return Vote.from(signed);
        } else if (obj instanceof Vote vote) {
            return vote;
        }
        throw new IllegalArgumentException("Unrecognized object: " + obj);
    }

    public static @NotNull List<Vote> votes(@NotNull Object @NotNull ... array) {
        return Arrays.stream(array).map(Vote::fromAny).toList();
    }

    public static @NotNull Vote up(int key) {
        return new Vote(Math.abs(key), 1);
    }

    public static @NotNull Vote down(int key) {
        return new Vote(Math.abs(key), -1);
    }

    public static @NotNull Vote none(int key) {
        return new Vote(Math.abs(key), 0);
    }
}
