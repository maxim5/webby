package io.webby.testing;

import com.google.common.truth.CustomSubjectBuilder;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.MapSubject;
import com.google.common.truth.Truth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

public class MoreTruth {
    public static <K, V> @NotNull MoreMapSubject<K, V> assertThat(@Nullable Map<K, V> map) {
        return Truth.assertAbout(MoreMapSubject.Builder::new).that(map);
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

        private static class Builder extends CustomSubjectBuilder {
            protected Builder(@NotNull FailureMetadata metadata) {
                super(metadata);
            }

            public <K, V> @NotNull MoreMapSubject<K, V> that(@Nullable Map<K, V> map) {
                return new MoreMapSubject<>(metadata(), map);
            }
        }
    }
}
