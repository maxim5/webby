package io.webby.orm.arch;

import com.google.common.base.Strings;
import io.webby.orm.api.annotate.ManyToMany;
import io.webby.util.reflect.EasyAnnotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record M2mInfo(@Nullable String leftField, @Nullable String rightField) {
    public static @Nullable M2mInfo fromModelClass(@NotNull Class<?> modelClass) {
        return EasyAnnotations.getOptionalAnnotation(modelClass, ManyToMany.class)
                .map(ann -> new M2mInfo(Strings.emptyToNull(ann.left()), Strings.emptyToNull(ann.right())))
                .orElse(null);
    }
}
