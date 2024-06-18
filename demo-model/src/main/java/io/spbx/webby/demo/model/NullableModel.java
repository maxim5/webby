package io.spbx.webby.demo.model;

import io.spbx.util.base.EasyNulls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Objects;

public record NullableModel(@Nullable String id,
                            @Nullable String str,
                            @Nullable Timestamp timestamp,
                            @Nullable char ch,
                            @Nullable Nested nested) {
    @Override
    public boolean equals(Object o) {
        return o instanceof NullableModel that &&
            Objects.equals(id, that.id) &&
            Objects.equals(str, that.str) &&
            Objects.equals(timestamp, that.timestamp) &&
            Objects.equals(ch, that.ch) &&
            Objects.equals(notNull(nested), notNull(that.nested));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, str, timestamp, notNull(nested));
    }

    // To make equals() work regardless whether a `nested` is not set or not loaded from DB. This simplifies the tests.
    private static @Nonnull Nested notNull(@Nullable Nested value) {
        return EasyNulls.firstNonNull(value, Nested.DEFAULT);
    }

    record Nested(int id, @Nullable String s) {
        private static final Nested DEFAULT = new Nested(0, null);
    }
}
