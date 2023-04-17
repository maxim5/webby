package io.webby.demo.model;

import io.webby.orm.api.annotate.Via;
import io.webby.util.collect.Pair;
import io.webby.util.func.Reversible;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public record MapperModel(int id,
                          @Via(PathMapper.class) List<String> path,
                          @Via(PairMapper.class) Pair<Long, Long> pair) {
    static class PathMapper implements Reversible<String, List<String>> {
        public static final PathMapper INSTANCE = new PathMapper();
        @Override
        public @NotNull List<String> forward(@NotNull String joint) {
            return Arrays.stream(joint.split("\\|")).toList();
        }
        @Override
        public @NotNull String backward(@NotNull List<String> split) {
            return String.join("|", split);
        }
    }

    static class PairMapper implements Reversible<Pair<Long, Long>, String> {
        @Override
        public @NotNull String forward(@NotNull Pair<Long, Long> pair) {
            return pair.mapToObj((x, y) -> x + ":" + y);
        }
        @Override
        public @NotNull Pair<Long, Long> backward(@NotNull String joint) {
            return Pair.of(joint.split(":")).map(Long::valueOf, Long::valueOf);
        }
    }
}
