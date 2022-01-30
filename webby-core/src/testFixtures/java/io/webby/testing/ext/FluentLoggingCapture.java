package io.webby.testing.ext;

import com.google.common.flogger.AbstractLogger;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.backend.LogData;
import com.google.common.flogger.backend.LoggerBackend;
import io.webby.util.base.Unchecked;
import io.webby.util.reflect.EasyMembers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;

public class FluentLoggingCapture implements BeforeEachCallback, AfterEachCallback {
    private final FluentLogger logger;
    private LoggerBackend backend;
    private List<LogData> logRecords;

    public FluentLoggingCapture(@NotNull Class<?> klass) {
        logger = Unchecked.Suppliers.runRethrow(() -> getFluentLogger(klass));
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        backend = extractBackend(logger);
        injectBackend(logger, buildMock(backend));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        injectBackend(logger, requireNonNull(backend));
    }

    public @NotNull List<LogData> logRecords() {
        return requireNonNull(logRecords);
    }

    public @NotNull List<LogData> logRecordsContaining(@NotNull String substr) {
        return logRecords().stream().filter(data -> formatMessage(data).contains(substr)).toList();
    }

    public @NotNull List<LogData> logRecordsMatching(@NotNull String regex) {
        Pattern pattern = Pattern.compile(regex);
        return logRecords().stream().filter(data -> pattern.matcher(formatMessage(data)).find()).toList();
    }

    public void assertNoRecords() {
        assertThat(logRecords).isEmpty();
    }

    private static @NotNull String formatMessage(@NotNull LogData data) {
        String message = data.getTemplateContext().getMessage();
        Object[] arguments = data.getArguments();
        return message.formatted(arguments);
    }

    private @NotNull LoggerBackend buildMock(@NotNull LoggerBackend instance) {
        logRecords = new ArrayList<>();
        LoggerBackend mock = Mockito.spy(instance);
        Mockito.doAnswer(invocation -> {
            logRecords.add(invocation.getArgument(0));
            return invocation.callRealMethod();
        }).when(mock).log(Mockito.any());
        return mock;
    }

    private static @NotNull LoggerBackend extractBackend(@NotNull FluentLogger logger) throws IllegalAccessException {
        Field field = EasyMembers.findField(AbstractLogger.class, "backend");
        Assertions.assertNotNull(field);
        field.setAccessible(true);
        return (LoggerBackend) field.get(logger);
    }

    private static void injectBackend(@NotNull FluentLogger logger,
                                      @NotNull LoggerBackend backend) throws IllegalAccessException {
        Field field = EasyMembers.findField(AbstractLogger.class, "backend");
        Assertions.assertNotNull(field);
        field.setAccessible(true);
        field.set(logger, backend);
    }

    private static @NotNull FluentLogger getFluentLogger(@NotNull Class<?> klass) throws IllegalAccessException {
        Field field = EasyMembers.findField(klass, it -> it.getType().equals(FluentLogger.class));
        Assertions.assertNotNull(field);
        field.setAccessible(true);
        return (FluentLogger) field.get(null);
    }
}
