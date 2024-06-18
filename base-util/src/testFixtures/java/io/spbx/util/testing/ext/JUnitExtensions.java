package io.spbx.util.testing.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;

import static io.spbx.util.base.EasyCast.castAny;

public class JUnitExtensions {
    // A workaround while arguments aren't passed through the public API
    // https://github.com/junit-team/junit5/issues/1139
    public static @Nullable Object[] extractInvocationArguments(@NotNull ExtensionContext context) {
        try {
            TestMethodTestDescriptor testDescriptor = invokeMethod(context, "getTestDescriptor");
            TestTemplateInvocationContext invocationContext = fieldValue(testDescriptor, "invocationContext");
            return fieldValue(invocationContext, "arguments");
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static <T> T invokeMethod(@NotNull Object object, @NotNull String methodName) {
        Method method = ReflectionUtils.findMethod(object.getClass(), methodName).orElseThrow();
        return castAny(ReflectionUtils.invokeMethod(method, object));
    }

    private static <T> T fieldValue(@NotNull Object object, @NotNull String fieldName) throws Exception {
        return castAny(ReflectionUtils.tryToReadFieldValue(castAny(object.getClass()), fieldName, object).get());
    }
}
