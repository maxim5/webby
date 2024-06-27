package io.spbx.webby.db.count.vote;

import io.spbx.util.base.EasyExceptions.IllegalArgumentExceptions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

record Vote(int key, int val) {
    public Vote {
        assertThat(key > 0).isTrue();
        assertThat(-1 <= val && val <= 1).isTrue();
    }

    public static @NotNull Vote from(int signed) {
        assertThat(signed != 0).isTrue();
        return new Vote(Math.abs(signed), signed > 0 ? 1 : -1);
    }

    public static @NotNull Vote fromAny(@NotNull Object obj) {
        if (obj instanceof Integer signed) {
            return Vote.from(signed);
        } else if (obj instanceof Vote vote) {
            return vote;
        }
        throw IllegalArgumentExceptions.format("Unrecognized object: %s", obj);
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
