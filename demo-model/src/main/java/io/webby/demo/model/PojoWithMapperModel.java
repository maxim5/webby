package io.webby.demo.model;

import io.spbx.orm.api.annotate.Sql.Via;
import io.spbx.util.func.Reversible;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public record PojoWithMapperModel(int id, Pojo pojo) {
    record Pojo(@Via(CoordinatesMapper.class) Point coordinates) {
    }

    static class CoordinatesMapper implements Reversible<Point, Long> {
        @Override
        public @NotNull Long forward(@NotNull Point point) {
            return (((long) point.x) << 32) | (point.y & 0xffffffffL);
        }
        @Override
        public @NotNull Point backward(@NotNull Long packed) {
            return new Point((int) (packed >> 32), packed.intValue());
        }
    }
}
