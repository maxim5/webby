package io.webby.orm.codegen;

import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.testing.orm.AssertCode;
import io.webby.testing.orm.AssertCode.JavaSubject;
import io.webby.testing.orm.AssertSql;
import io.webby.testing.orm.AssertSql.SqlSubject;
import org.jetbrains.annotations.NotNull;

class AssertSnippet {
    @CheckReturnValue
    public static @NotNull StringSubject assertThat(@NotNull Snippet snippet) {
        return Truth.assertThat(snippet.joinLines());
    }

    @CheckReturnValue
    public static @NotNull SqlSubject<SqlSubject<?>> assertThatSql(@NotNull Snippet snippet) {
        return AssertSql.assertThatSql(snippet.joinLines());
    }

    @CheckReturnValue
    public static @NotNull JavaSubject assertThatJava(@NotNull Snippet snippet) {
        return AssertCode.assertThatJava(snippet.joinLines());
    }
}
