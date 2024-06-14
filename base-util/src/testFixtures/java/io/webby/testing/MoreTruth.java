package io.webby.testing;

import com.google.common.truth.*;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.webby.util.base.EasyCast.castAny;
import static java.util.Objects.requireNonNull;

public class MoreTruth {
    @CheckReturnValue
    public static @NotNull StringSubject assertThat(@Nullable CharSequence charSequence) {
        return Truth.assertThat(charSequence != null ? charSequence.toString() : null);
    }

    @CheckReturnValue
    public static @NotNull MoreStringSubject assertThat(@Nullable String str) {
        return Truth.assertAbout(metadata -> new CustomSubjectBuilder(metadata) {
            @NotNull MoreStringSubject that() {
                return new MoreStringSubject(metadata, str);
            }
        }).that();
    }

    @CheckReturnValue
    public static <K, V> @NotNull MoreMapSubject<K, V> assertThat(@Nullable Map<K, V> map) {
        return Truth.assertAbout(metadata -> new CustomSubjectBuilder(metadata) {
            public @NotNull MoreMapSubject<K, V> that() {
                return new MoreMapSubject<>(metadata, map);
            }
        }).that();
    }

    @CheckReturnValue
    public static @NotNull MoreBooleanSubject assertThat(boolean actual) {
        return Truth.assertAbout(metadata -> new CustomSubjectBuilder(metadata) {
            @NotNull MoreBooleanSubject that() {
                return new MoreBooleanSubject(metadata, actual);
            }
        }).that();
    }

    @CheckReturnValue
    public static <T> @NotNull AlsoSubject<T> assertAlso(@Nullable T actual) {
        return new AlsoSubject<>(actual);
    }

    @CheckReturnValue
    public static @NotNull IntegerSubject assertThat(@Nullable FailureMetadata metadata, @Nullable Integer i) {
        return new IntegerSubject(metadata, i) {};
    }

    @CheckReturnValue
    public static @NotNull StringSubject assertThat(@Nullable FailureMetadata metadata, @Nullable String str) {
        return new StringSubject(metadata, str) {};
    }

    @CheckReturnValue
    public static @NotNull IterableSubject assertThat(@Nullable FailureMetadata metadata, @Nullable Iterable<?> iter) {
        return new IterableSubject(metadata, iter) {};
    }

    public static class MoreBooleanSubject extends Subject {
        private final FailureMetadata metadata;
        private final boolean actual;

        protected MoreBooleanSubject(@NotNull FailureMetadata metadata, boolean actual) {
            super(metadata, actual);
            this.metadata = metadata;
            this.actual = actual;
        }

        @CheckReturnValue
        public @NotNull BooleanSubject withMessage(String format, @Nullable Object... args) {
            return assert_(metadata).withMessage(format, args).that(actual);
        }
    }

    public static class MoreStringSubject extends StringSubject {
        private final FailureMetadata metadata;
        private final String str;

        protected MoreStringSubject(@Nullable FailureMetadata metadata, @Nullable String str) {
            super(metadata, str);
            this.metadata = metadata;
            this.str = str;
        }

        public @NotNull MoreStringSubject trimmed() {
            return str != null ? new MoreStringSubject(metadata, str.trim()) : this;
        }

        public void linesMatch(@Nullable String expected) {
            if (expected == null) {
                isNull();
            } else {
                linesMatch(expected.lines().toList());
            }
        }

        public void linesMatch(@Nullable Iterable<String> expected) {
            if (expected == null) {
                isNull();
            } else {
                isNotNull();
                assertThat(metadata, requireNonNull(str).lines().toList())
                    .containsExactlyElementsIn(expected)
                    .inOrder();
            }
        }
    }

    public static class MoreMapSubject<K, V> extends MapSubject {
        private final FailureMetadata metadata;
        private final Map<K, V> map;

        protected MoreMapSubject(@Nullable FailureMetadata metadata, @Nullable Map<K, V> map) {
            super(metadata, map);
            this.metadata = metadata;
            this.map = map;
        }

        public @NotNull MoreMapSubject<K, V> trimmed() {
            if (map == null) {
                return this;
            }
            Map<K, V> trimmed = map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new MoreMapSubject<>(metadata, trimmed);
        }
    }

    public record AlsoSubject<T>(@Nullable T actual) {
        public void isEquivalentTo(@Nullable T expected) {
            Truth.assertThat(actual).isEqualTo(expected);
            Truth.assertThat(Objects.toString(actual)).isEqualTo(Objects.toString(expected));
            Truth.assertThat(Objects.hashCode(actual)).isEqualTo(Objects.hashCode(expected));
        }
    }

    static @NotNull StandardSubjectBuilder assert_(@NotNull FailureMetadata metadata) {
        try {
            Constructor<StandardSubjectBuilder> constructor = castAny(StandardSubjectBuilder.class.getDeclaredConstructors()[0]);
            constructor.setAccessible(true);
            return constructor.newInstance(metadata);
        } catch (Throwable e) {
            return Unchecked.rethrow(e);
        }
    }
}
