package io.webby.db.content;

import org.jetbrains.annotations.NotNull;

public record FileFormat(@NotNull String form) {
    public FileFormat {
        assert !form.isEmpty() : "File form can't be empty";
    }
}
