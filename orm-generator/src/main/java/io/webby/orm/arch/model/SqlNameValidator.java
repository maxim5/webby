package io.webby.orm.arch.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.regex.Pattern;

public class SqlNameValidator {
    public static @NotNull String validateSqlName(@NotNull String name) {
        assert isValidSqlName(name) : "Invalid sql name: " + name;
        return name;
    }

    private static final Pattern SQL_NAME_PATTERN = Pattern.compile("^[a-zA-Z_]\\w*$");

    @VisibleForTesting
    static boolean isValidSqlName(@NotNull String name) {
        return SQL_NAME_PATTERN.matcher(name).matches();
    }
}
