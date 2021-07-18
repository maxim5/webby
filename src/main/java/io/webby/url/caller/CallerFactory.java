package io.webby.url.caller;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.Json;
import io.webby.url.Protobuf;
import io.webby.url.UrlConfigError;
import io.webby.url.impl.Binding;
import io.webby.url.validate.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Level;

public class CallerFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final IntValidator DEFAULT_INT = IntValidator.ANY;
    private static final StringValidator DEFAULT_STR = StringValidator.DEFAULT_256;
    private static final ImmutableMap<Class<?>, SimpleConverter<?>> DEFAULT_CONVERTERS =
            ImmutableMap.<Class<?>, SimpleConverter<?>>builder()
            .put(int.class, DEFAULT_INT.asSimpleConverter())
            .put(Integer.class, DEFAULT_INT.asSimpleConverter())
            .put(long.class, Long::parseLong)
            .put(Long.class, Long::valueOf)
            .put(short.class, Short::parseShort)
            .put(Short.class, Short::valueOf)
            .put(byte.class, Byte::parseByte)
            .put(Byte.class, Byte::valueOf)
            .put(char.class, s -> (char) Integer.parseInt(s))
            .put(Character.class, s -> (char) Integer.parseInt(s))
            .put(float.class, Float::parseFloat)
            .put(Float.class, Float::valueOf)
            .put(double.class, Double::parseDouble)
            .put(Double.class, Double::valueOf)
            .put(boolean.class, Boolean::parseBoolean)
            .put(Boolean.class, Boolean::valueOf)
            .put(CharSequence.class, DEFAULT_STR.asSimpleConverter())
            .put(String.class, DEFAULT_STR.asSimpleConverter())
            .build();

    @Inject private Injector injector;
    @Inject private ContentProviderFactory contentProviderFactory;

    @NotNull
    public Caller create(@NotNull Object instance,
                         @NotNull Binding binding,
                         @NotNull Map<String, Validator> validators,
                         @NotNull List<String> vars) {
        Method method = binding.method();

        Caller caller = tryCreateNativeCaller(instance, method, binding, validators, vars);
        if (caller != null) {
            return caller;
        }

        List<CallArgumentFunction> mapping = tryCreateGenericMapping(method, binding, validators, vars);
        if (mapping != null) {
            return new GenericCaller(instance, method, mapping);
        }

        throw new UrlConfigError("Failed to recognize method signature: %s".formatted(method));
    }

    @VisibleForTesting
    @Nullable
    Caller tryCreateNativeCaller(@NotNull Object instance,
                                 @NotNull Method method,
                                 @NotNull Binding binding,
                                 @NotNull Map<String, Validator> validators,
                                 @NotNull List<String> vars) {
        Parameter[] parameters = method.getParameters();

        boolean wantsRequest = false;
        if (parameters.length > 0 && canPassHttpRequest(parameters[0].getType())) {
            wantsRequest = true;
            parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
        }

        ContentProvider contentProvider = null;
        if (parameters.length > 0) {
            int contentIndex = parameters.length - 1;  // last
            Parameter parameter = parameters[contentIndex];
            if (isContentParam(parameter, binding)) {
                contentProvider = contentProviderFactory.getContentProvider(binding.options(), parameter.getType());
                parameters = Arrays.copyOfRange(parameters, 0, contentIndex);
            }
        }

        CallOptions options = new CallOptions(wantsRequest, contentProvider);

        if (parameters.length == 0) {
            assertVarsMatchParams(vars, 0, method);
            return new EmptyCaller(instance, method, options);
        }

        if (parameters.length == 1) {
            Class<?> type = parameters[0].getType();

            if (type.equals(Map.class)) {
                return new MapCaller(instance, method, validators, options);
            }
            if (!isNativeSupport(type)) {
                return null;
            }

            assertVarsMatchParams(vars, 1, method);
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

            if (!(isNativeSupport(type1) && isNativeSupport(type2))) {
                return null;
            }

            assertVarsMatchParams(vars, 2, method);
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
        return null;
    }

    private static void assertVarsMatchParams(@NotNull List<String> vars, int params, @NotNull Method method) {
        UrlConfigError.failIf(
                vars.size() < params,
                "Method %s has extra arguments that can't be matched to URL variables: %s".formatted(method, vars)
        );
        UrlConfigError.failIf(
                vars.size() > params,
                "Method %s does not accept enough arguments for requested URL variables: %s".formatted(method, vars)
        );
    }

    @VisibleForTesting
    @Nullable
    List<CallArgumentFunction> tryCreateGenericMapping(@NotNull Method method,
                                                       @NotNull Binding binding,
                                                       @NotNull Map<String, Validator> validators,
                                                       @NotNull List<String> vars) {
        Parameter[] params = method.getParameters();  // forget about previous guesses
        List<CallArgumentFunction> argMapping = new ArrayList<>(params.length);
        Queue<String> varz = new ArrayDeque<>(vars);
        int last = params.length - 1;

        for (int i = 0; i <= last; i++) {
            Class<?> type = params[i].getType();

            if (isAllowedToInject(type)) {
                try {
                    Object injectorInstance = injector.getInstance(type);
                    argMapping.add((request, varsMap) -> injectorInstance);
                    continue;
                } catch (Exception ignore) {}
            }

            if (canPassHttpRequest(type)) {
                argMapping.add((request, varsMap) -> request);
                continue;
            }

            if (!varz.isEmpty()) {
                String var = varz.peek();
                Validator validator = validators.computeIfAbsent(var, (k) -> DEFAULT_CONVERTERS.get(type));
                if (validator instanceof Converter<?> converter) {
                    varz.poll();
                    argMapping.add(((request, varsMap) -> {
                        try {
                            return converter.convert(varsMap.get(var));
                        } catch (ValidationError e) {
                            throw e;
                        } catch (RuntimeException e) {
                            throw new ValidationError("Failed to validate %s".formatted(var), e);
                        }
                    }));
                    continue;
                }
            }

            if (i == last && isContentParam(params[i], binding)) {
                ContentProvider provider = contentProviderFactory.getContentProvider(binding.options(), type);
                if (provider != null) {
                    argMapping.add((request, varsMap) -> provider.getContent(request));
                }
            }
        }

        if (argMapping.size() < params.length) {
            log.at(Level.FINE).log("Generic caller could match only %d arguments out of %d for method %s"
                    .formatted(argMapping.size(), params.length, method));
            return null;
        }
        if (!varz.isEmpty()) {
            log.at(Level.FINE).log("Method %s does not accept enough arguments for requested URL variables=%s"
                    .formatted(method, varz));
            return null;
        }

        return argMapping;
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
    static boolean isNativeSupport(Class<?> type) {
        return isInt(type) || isStringLike(type);
    }

    @VisibleForTesting
    static boolean isAllowedToInject(Class<?> type) {
        return !type.isPrimitive() && !isStringLike(type);
    }

    @VisibleForTesting
    static boolean canPassHttpRequest(Class<?> type) {
        return HttpRequest.class.isAssignableFrom(type);
    }

    @VisibleForTesting
    static boolean canPassContent(Class<?> type) {
        return !type.isPrimitive() && !isStringLike(type);
    }

    @VisibleForTesting
    static boolean canPassBuffer(Class<?> type) {
        return type.isAssignableFrom(CharBuffer.class) && !type.equals(Object.class);
    }

    @VisibleForTesting
    static boolean isContentParam(@NotNull Parameter parameter, @NotNull Binding binding) {
        return wantsContent(parameter) || canPassContent(parameter.getType()) && binding.options().expectsContent();
    }

    @VisibleForTesting
    static boolean wantsContent(@NotNull Parameter parameter) {
        return parameter.isAnnotationPresent(Json.class) || parameter.isAnnotationPresent(Protobuf.class);
    }
}
