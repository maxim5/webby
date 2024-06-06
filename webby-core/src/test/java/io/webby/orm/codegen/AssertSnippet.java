package io.webby.orm.codegen;

import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import io.webby.testing.orm.AssertSql;
import io.webby.testing.orm.AssertSql.SqlSubject;
import org.jetbrains.annotations.NotNull;

class AssertSnippet {
    public static @NotNull StringSubject assertThat(@NotNull Snippet snippet) {
        return Truth.assertThat(snippet.joinLines());
    }

    public static @NotNull SqlSubject<SqlSubject<?>> assertThatSql(@NotNull Snippet snippet) {
        return AssertSql.assertThatSql(snippet.joinLines());
    }
}
