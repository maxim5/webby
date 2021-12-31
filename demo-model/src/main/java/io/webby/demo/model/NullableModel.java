package io.webby.demo.model;

import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

public record NullableModel(@Nullable String id,
                            @Nullable String str,
                            @Nullable Timestamp timestamp,
                            @Nullable Nested nested) {

    record Nested(int id, @Nullable String s) {}
}
