package io.webby.testing;

import com.google.common.truth.*;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.stream.Collectors;

import static io.webby.util.base.EasyCast.castAny;

public class MoreTruth {
    @CheckReturnValue
    public static @NotNull StringSubject assertThat(@Nullable FailureMetadata metadata, @Nullable String str) {
        return new StringSubject(metadata, str) {};
    }

    @CheckReturnValue
    public static @NotNull IterableSubject assertThat(@Nullable FailureMetadata metadata, @Nullable Iterable<?> iter) {
        return new IterableSubject(metadata, iter) {};
    }

    @CheckReturnValue
    public static @NotNull MoreStringSubject assertThat(@Nullable String str) {
        return Truth.assertAbout(metadata -> new CustomSubjectBuilder(metadata) {
            @NotNull MoreStringSubject that(@Nullable String str) {
                return new MoreStringSubject(metadata, str);
            }
        }).that(str);
    }

    @CheckReturnValue
    public static <K, V> @NotNull MoreMapSubject<K, V> assertThat(@Nullable Map<K, V> map) {
        return Truth.assertAbout(metadata -> new CustomSubjectBuilder(metadata) {
            public @NotNull MoreMapSubject<K, V> that(@Nullable Map<K, V> map) {
                return new MoreMapSubject<>(metadata, map);
            }
        }).that(map);
    }

    public static class MoreStringSubject extends StringSubject {
        private final FailureMetadata metadata;
        private final String str;

        protected MoreStringSubject(FailureMetadata metadata, @Nullable String str) {
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
                assert str != null : "Impossible: `str` is already checked for null";
                assertThat(metadata, str.lines().toList())
                    .containsExactlyElementsIn(expected)
                    .inOrder();
            }
        }
    }

    public static class MoreMapSubject<K, V> extends MapSubject {
        private final FailureMetadata metadata;
        private final Map<K, V> map;

        protected MoreMapSubject(FailureMetadata metadata, @Nullable Map<K, V> map) {
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
