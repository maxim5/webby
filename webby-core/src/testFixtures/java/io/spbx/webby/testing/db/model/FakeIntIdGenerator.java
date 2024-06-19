package io.spbx.webby.testing.db.model;

import com.carrotsearch.hppc.IntContainer;
import io.spbx.util.hppc.EasyHppc;
import io.spbx.webby.db.model.IntIdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;

public class FakeIntIdGenerator implements IntIdGenerator {
    private final Iterator<Integer> idSequence;

    public FakeIntIdGenerator(@NotNull Iterator<Integer> idSequence) {
        this.idSequence = idSequence;
    }

    public static @NotNull FakeIntIdGenerator from(int ... ints) {
        return from(IntStream.of(ints).boxed().toList());
    }

    public static @NotNull FakeIntIdGenerator from(@NotNull IntContainer ints) {
        return from(EasyHppc.toJavaList(ints));
    }

    public static @NotNull FakeIntIdGenerator from(@NotNull Iterable<Integer> ints) {
        return new FakeIntIdGenerator(ints.iterator());
    }

    public static @NotNull FakeIntIdGenerator range(int start, int end) {
        return from(IntStream.range(start, end).boxed().toList());
    }

    @Override
    public int nextId() {
        assertThat(idSequence.hasNext()).isTrue();
        return idSequence.next();
    }
}
