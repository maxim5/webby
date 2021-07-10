package io.webby.url.caller;

import io.routekit.util.CharBuffer;
import io.webby.url.Binding;
import io.webby.url.UrlConfigError;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.StringValidator;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

// TODO: handle default values
public class CallerFactory {
    private static final IntValidator DEFAULT_INT = IntValidator.ANY;
    private static final StringValidator DEFAULT_STR = StringValidator.DEFAULT_256;

    @NotNull
    public Caller create(@NotNull Object instance,
                         @NotNull Binding binding,
                         @NotNull Map<String, Validator> validators,
                         @NotNull List<String> vars) {
        Method method = binding.method();
        Parameter[] parameters = method.getParameters();

        if (parameters.length == 0) {
            UrlConfigError.failIf(vars.size() > 0, "Incomplete method %s arguments: variables=%s".formatted(method, vars));
            return new EmptyCaller(instance, method);
        }

        if (parameters.length == 1) {
            UrlConfigError.failIf(vars.size() > 1, "Incomplete method %s arguments: variables=%s".formatted(method, vars));
            String var = vars.get(0);
            Class<?> type = parameters[0].getType();
            if (type.equals(int.class)) {
                IntValidator validator = getValidator(validators, var, DEFAULT_INT);
                return new IntCaller(instance, method, validator, var);
            }
            if (type.equals(String.class)) {
                StringValidator validator = getValidator(validators, var, DEFAULT_STR);
                return new StringCaller(instance, method, validator, var);
            }
            if (type.equals(CharBuffer.class)) {
                StringValidator validator = getValidator(validators, var, DEFAULT_STR);
                return new CharBufferCaller(instance, method, validator, var);
            }
            if (type.equals(Map.class)) {
                return new MapCaller(instance, method, validators);
            }
        }

        throw new UrlConfigError("Failed to recognize method signature: %s".formatted(method));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Validator> T getValidator(Map<String, Validator> validators, String name, T def) {
        try {
            Validator validator = validators.getOrDefault(name, def);
            return (T) validator;
        } catch (ClassCastException e) {
            throw new UrlConfigError("%s validator must be %s".formatted(name, def.getClass()));
        }
    }
}
