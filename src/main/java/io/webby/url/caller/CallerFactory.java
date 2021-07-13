package io.webby.url.caller;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.UrlConfigError;
import io.webby.url.impl.Binding;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.StringValidator;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CallerFactory {
    private static final IntValidator DEFAULT_INT = IntValidator.ANY;
    private static final StringValidator DEFAULT_STR = StringValidator.DEFAULT_256;

    @Inject private ContentProviderFactory contentProviderFactory;

    @NotNull
    public Caller create(@NotNull Object instance,
                         @NotNull Binding binding,
                         @NotNull Map<String, Validator> validators,
                         @NotNull List<String> vars) {
        Method method = binding.method();
        Parameter[] parameters = method.getParameters();

        boolean wantsRequest = false;
        if (parameters.length > 0 && canPassHttpRequest(parameters[0].getType())) {
            wantsRequest = true;
            parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
        }

        ContentProvider contentProvider = null;
        if (binding.options().wantsContent() && parameters.length > 0) {
            int contentIndex = parameters.length - 1;  // last
            Class<?> type = parameters[contentIndex].getType();
            if (!type.isPrimitive() && !isStringLike(type)) {
                contentProvider = contentProviderFactory.getContentProvider(binding.options(), type);
                parameters = Arrays.copyOfRange(parameters, 0, contentIndex);
            }
        }

        CallOptions options = new CallOptions(wantsRequest, contentProvider);

        if (parameters.length == 0) {
            UrlConfigError.failIf(vars.size() != 0,
                    "Method %s does not accept enough arguments for requested URL variables=%s".formatted(method, vars));
            return new EmptyCaller(instance, method, options);
        }

        if (parameters.length == 1) {
            Class<?> type = parameters[0].getType();

            if (type.equals(Map.class)) {
                return new MapCaller(instance, method, validators, options);
            }

            UrlConfigError.failIf(vars.size() != 1,
                    "Method %s does not accept enough arguments for requested URL variables=%s".formatted(method, vars));
            String var = vars.get(0);

            if (isInt(type)) {
                IntValidator validator = getValidator(validators, var, DEFAULT_INT);
                return new IntCaller(instance, method, validator, var, options);
            }

            if (isStringLike(type)) {
                StringValidator validator = getValidator(validators, var, DEFAULT_STR);
                return new StringCaller(instance, method, validator, var, options.toRichStr(canPassBuffer(type)));
            }
        }

        if (parameters.length == 2) {
            Class<?> type1 = parameters[0].getType();
            Class<?> type2 = parameters[1].getType();

            UrlConfigError.failIf(vars.size() != 2,
                    "Method %s does not accept enough arguments for requested URL variables=%s".formatted(method, vars));
            String var1 = vars.get(0);
            String var2 = vars.get(1);

            if (isInt(type1) && isInt(type2)) {
                IntValidator validator1 = getValidator(validators, var1, DEFAULT_INT);
                IntValidator validator2 = getValidator(validators, var2, DEFAULT_INT);
                return new IntIntCaller(instance, method, validator1, validator2, var1, var2, options);
            }

            if (isInt(type1) && isStringLike(type2)) {
                IntValidator intValidator = getValidator(validators, var1, DEFAULT_INT);
                StringValidator strValidator = getValidator(validators, var2, DEFAULT_STR);
                return new IntStrCaller(instance, method, intValidator, strValidator, var1, var2,
                        options.toRichStrInt(canPassBuffer(type2), false));
            }

            if (isStringLike(type1) && isInt(type2)) {
                StringValidator strValidator = getValidator(validators, var1, DEFAULT_STR);
                IntValidator intValidator = getValidator(validators, var2, DEFAULT_INT);
                return new IntStrCaller(instance, method, intValidator, strValidator, var2, var1,
                        options.toRichStrInt(canPassBuffer(type1), true));
            }

            if (isStringLike(type1) && isStringLike(type2)) {
                StringValidator validator1 = getValidator(validators, var1, DEFAULT_STR);
                StringValidator validator2 = getValidator(validators, var2, DEFAULT_STR);
                return new StrStrCaller(instance, method, validator1, validator2, var1, var2,
                        options.toRichStrStr(canPassBuffer(type1), canPassBuffer(type2)));
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

    @VisibleForTesting
    static boolean isInt(Class<?> type) {
        return type.equals(int.class);
    }

    @VisibleForTesting
    static boolean isStringLike(Class<?> type) {
        return CharSequence.class.isAssignableFrom(type);
    }

    @VisibleForTesting
    static boolean canPassHttpRequest(Class<?> type) {
        return HttpRequest.class.isAssignableFrom(type);
    }

    @VisibleForTesting
    static boolean canPassBuffer(Class<?> type) {
        return type.isAssignableFrom(CharBuffer.class) && !type.equals(Object.class);
    }
}
