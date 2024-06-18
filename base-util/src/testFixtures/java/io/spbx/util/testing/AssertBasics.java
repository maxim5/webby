package io.spbx.util.testing;

import io.spbx.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class AssertBasics {
    public static <T> void assertPrivateFieldValue(@NotNull T object, @NotNull String name, @NotNull Object expected) {
        assertThat(getPrivateFieldValue(object, name)).isEqualTo(expected);
    }

    public static <T> void assertPrivateFieldClass(@NotNull T object, @NotNull String name, @NotNull Class<?> expected) {
        Object value = getPrivateFieldValue(object, name);
        assertThat(value).isNotNull();
        assertThat(value.getClass()).isEqualTo(expected);
    }

    public static <T> Object getPrivateFieldValue(@NotNull T object, @NotNull String name) {
        List<Field> fields = ReflectionUtils.findFields(object.getClass(),
                                                        field -> field.getName().equals(name),
                                                        ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP);
        assertThat(fields).hasSize(1);
        Field field = fields.getFirst();
        field.setAccessible(true);
        return Unchecked.Suppliers.rethrow(() -> field.get(object)).get();
    }
}
