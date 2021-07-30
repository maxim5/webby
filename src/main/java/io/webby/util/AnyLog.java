package io.webby.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;

public final class AnyLog {
    @CanIgnoreReturnValue
    public static boolean setLogLevelAll(Object level) {
        return Log4j2.setAllLevels(level) || JavaLogging.setRootLogLevel(level);
    }

    public static class JavaLogging {
        public static boolean setRootLogLevel(Object level) {
            LogManager.getLogManager().getLogger("").setLevel(getLevel(level));
            return true;
        }

        public static Level getLevel(Object level) {
            return level instanceof Level ? (Level) level : Level.parse(level.toString());
        }
    }

    public static class Log4j2 {
        private static final String CONFIGURATOR = "org.apache.logging.log4j.core.config.Configurator";
        private static final String LOG_MANAGER = "org.apache.logging.log4j.LogManager";
        private static final String LEVEL = "org.apache.logging.log4j.Level";
        private static final String ROOT_LOGGER_NAME = "";

        @CanIgnoreReturnValue
        public static boolean setAllLevels(Object level) {
            return callStatic(CONFIGURATOR, "setAllLevels", ROOT_LOGGER_NAME, getLevel(level)) != null;
        }

        public static Object getLevel(Object level) {
            if (level instanceof String) {
                return callStatic(LEVEL, "forName", level, 0);
            }
            if (level instanceof Integer) {
                return callStatic(LEVEL, "forName", String.valueOf(level), level);
            }
            if (level instanceof Level lvl) {
                int value = switch (lvl.getName()) {
                    case "OFF" -> 0;        // OFF
                    case "SEVERE" -> 100;   // FATAL
                    case "WARNING" -> 300;  // WARN
                    case "INFO" -> 400;     // INFO
                    case "CONFIG" -> 500;   // DEBUG
                    case "FINE" -> 600;     // TRACE
                    case "FINER" -> 600;    // TRACE
                    case "ALL" -> Integer.MAX_VALUE;
                    default -> 1000 - lvl.intValue();
                };
                return callStatic(LEVEL, "forName", lvl.getName(), value);
            }
            return level;
        }
    }

    @Nullable
    private static Object callStatic(@NotNull String className, @NotNull String methodName, Object ... args) {
        Method method = findMethod(className, methodName);
        if (method != null) {
            try {
                method.setAccessible(true);
                Object result = method.invoke(null, args);
                if (result == null && method.getReturnType().equals(Void.TYPE)) {
                    return Void.TYPE;
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    private static Method findMethod(@NotNull String className, @NotNull String methodName) {
        return findMethod(classForNameOrNull(className), methodName);
    }

    @Nullable
    private static Class<?> classForNameOrNull(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    @Nullable
    private static Method findMethod(@Nullable Class<?> klass, @NotNull String name) {
        return klass == null ? null :
                Arrays.stream(klass.getDeclaredMethods())
                        .filter(method -> method.getName().equals(name))
                        .findAny()
                        .orElse(null);
    }
}
