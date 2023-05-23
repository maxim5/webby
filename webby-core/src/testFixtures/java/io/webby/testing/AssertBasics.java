package io.webby.testing;

import io.webby.util.base.Unchecked;
import io.webby.util.collect.EasyMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AssertBasics {
    @SuppressWarnings("unchecked")
    public static <T> void assertOneOf(@Nullable T value, @Nullable T @NotNull ... expected) {
        assertThat(value).isIn(Arrays.asList(expected));
    }

    public static <T> void assertOneOf(@Nullable T value, @NotNull Iterable<T> expected) {
        assertThat(value).isIn(expected);
    }

    public static void assertMapContents(@NotNull Map<?, ?> map, @Nullable Object @NotNull ... expected) {
        LinkedHashMap<Object, Object> expectedMap = EasyMaps.asMap(expected);
        Set<Object> keys = Stream.concat(map.keySet().stream(), expectedMap.keySet().stream()).collect(Collectors.toSet());
        for (Object key : keys) {
            assertEquals(expectedMap.get(key), map.get(key));
        }
    }

    public static <T> void assertPrivateFieldValue(@NotNull T object, @NotNull String name, @NotNull Object expected) {
        assertEquals(expected, getPrivateFieldValue(object, name));
    }

    public static <T> void assertPrivateFieldClass(@NotNull T object, @NotNull String name, @NotNull Class<?> expected) {
        Object value = getPrivateFieldValue(object, name);
        assertNotNull(value);
        assertEquals(expected, value.getClass());
    }

    public static <T> Object getPrivateFieldValue(@NotNull T object, @NotNull String name) {
        List<Field> fields = ReflectionUtils.findFields(object.getClass(),
                                                        field -> field.getName().equals(name),
                                                        ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP);
        assertThat(fields).hasSize(1);
        Field field = fields.get(0);
        field.setAccessible(true);
        return Unchecked.Suppliers.rethrow(() -> field.get(object)).get();
    }
}
