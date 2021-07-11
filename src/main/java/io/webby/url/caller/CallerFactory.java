package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.impl.Binding;
import io.webby.url.UrlConfigError;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.StringValidator;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
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

        boolean wantsRequest = false;
        if (parameters.length > 0 && parameters[0].getType().isAssignableFrom(FullHttpRequest.class)) {
            wantsRequest = true;
            parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
        }

        if (parameters.length == 0) {
            UrlConfigError.failIf(vars.size() != 0, "Incomplete method %s arguments: variables=%s".formatted(method, vars));
            return new EmptyCaller(instance, method, wantsRequest);
        }

        if (parameters.length == 1) {
            Class<?> type = parameters[0].getType();

            if (type.equals(Map.class)) {
                return new MapCaller(instance, method, validators, wantsRequest);
            }

            UrlConfigError.failIf(vars.size() != 1, "Incomplete method %s arguments: variables=%s".formatted(method, vars));
            String var = vars.get(0);

            if (isInt(type)) {
                IntValidator validator = getValidator(validators, var, DEFAULT_INT);
                return new IntCaller(instance, method, validator, var, wantsRequest);
            }

            if (isStringLike(type)) {
                StringValidator validator = getValidator(validators, var, DEFAULT_STR);
                return new StringCaller(instance, method, validator, var, wantsRequest, canPassBuffer(type));
            }
        }

        if (parameters.length == 2) {
            Class<?> type1 = parameters[0].getType();
            Class<?> type2 = parameters[1].getType();

            UrlConfigError.failIf(vars.size() != 2, "Incomplete method %s arguments: variables=%s".formatted(method, vars));
            String var1 = vars.get(0);
            String var2 = vars.get(1);

            if (isInt(type1) && isInt(type2)) {
                IntValidator validator1 = getValidator(validators, var1, DEFAULT_INT);
                IntValidator validator2 = getValidator(validators, var2, DEFAULT_INT);
                return new IntIntCaller(instance, method, validator1, validator2, var1, var2, wantsRequest);
            }

            if (isInt(type1) && isStringLike(type2)) {
                IntValidator intValidator = getValidator(validators, var1, DEFAULT_INT);
                StringValidator strValidator = getValidator(validators, var2, DEFAULT_STR);
                return new IntStrCaller(instance, method, intValidator, strValidator, var1, var2,
                        wantsRequest, canPassBuffer(type2), false);
            }

            if (isStringLike(type1) && isInt(type2)) {
                StringValidator strValidator = getValidator(validators, var1, DEFAULT_STR);
                IntValidator intValidator = getValidator(validators, var2, DEFAULT_INT);
                return new IntStrCaller(instance, method, intValidator, strValidator, var2, var1,
                        wantsRequest, canPassBuffer(type1), true);
            }

            if (isStringLike(type1) && isStringLike(type2)) {
                StringValidator validator1 = getValidator(validators, var1, DEFAULT_STR);
                StringValidator validator2 = getValidator(validators, var2, DEFAULT_STR);
                return new StrStrCaller(instance, method, validator1, validator2, var1, var2,
                        wantsRequest, canPassBuffer(type1), canPassBuffer(type2));
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

    private static boolean isInt(Class<?> type) {
        return type.equals(int.class);
    }

    private static boolean isStringLike(Class<?> type) {
        return CharSequence.class.isAssignableFrom(type);
    }

    private static boolean canPassBuffer(Class<?> type) {
        return type.isAssignableFrom(CharBuffer.class);
    }
}
