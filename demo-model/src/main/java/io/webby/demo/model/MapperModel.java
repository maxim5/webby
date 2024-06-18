package io.webby.demo.model;

import com.carrotsearch.hppc.IntArrayList;
import io.webby.db.codec.standard.Instant96Codec;
import io.webby.db.codec.standard.IntArrayListCodec;
import io.webby.orm.adapter.BytesMapper;
import io.webby.orm.api.annotate.Sql;
import io.webby.orm.api.annotate.Sql.Via;
import io.spbx.util.base.Pair;
import io.spbx.util.func.Reversible;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public record MapperModel(int id,
                          @Via(PathMapper.class) @NotNull List<String> path,
                          @Sql(via = PairMapper.class) @NotNull Pair<Long, Long> pair,
                          @Via(IntArrayListCodec.class) @NotNull IntArrayList ints,
                          @Via(Instant96Codec.class) @NotNull Instant time,
                          @Via(BitSetMapper.class) @NotNull BitSet bits) {
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

    static class BitSetMapper extends BytesMapper<BitSet> {}
}
