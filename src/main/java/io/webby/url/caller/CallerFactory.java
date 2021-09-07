package io.webby.url.caller;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharArray;
import io.webby.app.AppConfigException;
import io.webby.url.HandlerConfigError;
import io.webby.url.annotate.Json;
import io.webby.url.annotate.Protobuf;
import io.webby.url.convert.Constraint;
import io.webby.url.convert.Converter;
import io.webby.url.convert.IntConverter;
import io.webby.url.convert.StringConverter;
import io.webby.url.handle.Handler;
import io.webby.url.handle.IntHandler;
import io.webby.url.handle.StringHandler;
import io.webby.url.impl.Binding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Level;

import static io.webby.url.HandlerConfigError.failIf;

public class CallerFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final IntConverter DEFAULT_INT = IntConverter.ANY;
    private static final StringConverter DEFAULT_STR = StringConverter.MAX_256;
    private static final ImmutableMap<Class<?>, Converter<?>> DEFAULT_CONVERTERS =
            ImmutableMap.<Class<?>, Converter<?>>builder()
            .put(int.class, DEFAULT_INT)
            .put(Integer.class, DEFAULT_INT)
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
            .put(CharSequence.class, DEFAULT_STR)
            .put(String.class, DEFAULT_STR)
            .build();

    @Inject private Injector injector;
    @Inject private ContentProviderFactory contentProviderFactory;

    public @NotNull Caller create(@NotNull Object instance,
                                  @NotNull Binding binding,
                                  @NotNull Map<String, Constraint<?>> constraints,
                                  @NotNull List<String> vars) throws AppConfigException {
        Method method = binding.method();

        Caller nativeCaller = tryCreateNativeCaller(instance, method, constraints, vars);
        if (nativeCaller != null) {
            return nativeCaller;
        }

        Caller optimizedCaller = tryCreateOptimizedCaller(instance, method, binding, constraints, vars);
        if (optimizedCaller != null) {
            return optimizedCaller;
        }

        List<CallArgumentFunction> mapping = tryCreateGenericMapping(method, binding, constraints, vars);
        if (mapping != null) {
            return new GenericCaller(instance, method, mapping);
        }

        throw new HandlerConfigError("Failed to recognize method signature: %s", method);
    }

    @VisibleForTesting
    @Nullable Caller tryCreateNativeCaller(@NotNull Object instance,
                                           @NotNull Method method,
                                           @NotNull Map<String, Constraint<?>> constraints,
                                           @NotNull List<String> vars) {
        if (instance instanceof Handler<?> handler && isMethodImplementsInterface(method, Handler.class)) {
            return new NativeCaller(handler);
        }

        if (instance instanceof IntHandler<?> handler && isMethodImplementsInterface(method, IntHandler.class)) {
            assertVarsMatchParams(vars, 1, method);
            String var = vars.get(0);
            IntConverter validator = getConstraint(constraints, var, DEFAULT_INT);
            return new NativeIntCaller(handler, validator, var);
        }

        if (instance instanceof StringHandler<?> handler && isMethodImplementsInterface(method, StringHandler.class)) {
            assertVarsMatchParams(vars, 1, method);
            String var = vars.get(0);
            StringConverter validator = getConstraint(constraints, var, DEFAULT_STR);
            return new NativeStringCaller(handler, validator, var);
        }

        return null;
    }

    private boolean isMethodImplementsInterface(@NotNull Method method, @NotNull Class<?> interfaceClass) {
        assert Modifier.isInterface(interfaceClass.getModifiers()) : "Expected the interface, but got " + interfaceClass;
        Method[] methods = interfaceClass.getDeclaredMethods();
        assert methods.length == 1 : "Internal error: the interface declares several methods";
        Method declared = methods[0];
        return method.getName().equals(declared.getName()) &&
                Arrays.equals(method.getParameterTypes(), declared.getParameterTypes());
    }

    @VisibleForTesting
    @Nullable Caller tryCreateOptimizedCaller(@NotNull Object instance,
                                              @NotNull Method method,
                                              @NotNull Binding binding,
                                              @NotNull Map<String, Constraint<?>> constraints,
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
                return new MapCaller(instance, method, constraints, options);
            }
            if (!isNativeSupport(type)) {
                return null;
            }

            assertVarsMatchParams(vars, 1, method);
            String var = vars.get(0);

            if (isInt(type)) {
                IntConverter validator = getConstraint(constraints, var, DEFAULT_INT);
                return new IntCaller(instance, method, validator, var, options);
            }

            if (isStringLike(type)) {
                StringConverter validator = getConstraint(constraints, var, DEFAULT_STR);
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
                IntConverter validator1 = getConstraint(constraints, var1, DEFAULT_INT);
                IntConverter validator2 = getConstraint(constraints, var2, DEFAULT_INT);
                return new IntIntCaller(instance, method, validator1, validator2, var1, var2, options);
            }

            if (isInt(type1) && isStringLike(type2)) {
                IntConverter intConverter = getConstraint(constraints, var1, DEFAULT_INT);
                StringConverter strValidator = getConstraint(constraints, var2, DEFAULT_STR);
                return new IntStrCaller(instance, method, intConverter, strValidator, var1, var2,
                                        options.toRichStrInt(canPassBuffer(type2), false));
            }

            if (isStringLike(type1) && isInt(type2)) {
                StringConverter strValidator = getConstraint(constraints, var1, DEFAULT_STR);
                IntConverter intConverter = getConstraint(constraints, var2, DEFAULT_INT);
                return new IntStrCaller(instance, method, intConverter, strValidator, var2, var1,
                                        options.toRichStrInt(canPassBuffer(type1), true));
            }

            if (isStringLike(type1) && isStringLike(type2)) {
                StringConverter validator1 = getConstraint(constraints, var1, DEFAULT_STR);
                StringConverter validator2 = getConstraint(constraints, var2, DEFAULT_STR);
                return new StrStrCaller(instance, method, validator1, validator2, var1, var2,
                                        options.toRichStrStr(canPassBuffer(type1), canPassBuffer(type2)));
            }
        }

        return null;
    }

    private static void assertVarsMatchParams(@NotNull List<String> vars, int params, @NotNull Method method) {
        failIf(vars.size() < params,
               "Method %s has extra arguments that can't be matched to URL variables: %s", method, vars);
        failIf(vars.size() > params,
               "Method %s does not accept enough arguments for requested URL variables: %s", method, vars);
    }

    @VisibleForTesting
    @Nullable List<CallArgumentFunction> tryCreateGenericMapping(@NotNull Method method,
                                                                 @NotNull Binding binding,
                                                                 @NotNull Map<String, Constraint<?>> constraints,
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
                String var = varz.poll();
                Constraint<?> constraint = constraints.getOrDefault(var, DEFAULT_CONVERTERS.get(type));
                argMapping.add(((request, varsMap) -> constraint.applyWithName(var, varsMap.get(var))));
                continue;
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
    private static <T extends Constraint<?>> T getConstraint(Map<String, Constraint<?>> constraints, String name, T def) {
        try {
            Constraint<?> constraint = constraints.getOrDefault(name, def);
            return (T) constraint;
        } catch (ClassCastException e) {
            throw new HandlerConfigError("%s validator must be %s".formatted(name, def.getClass()), e);
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
        return type.isAssignableFrom(CharArray.class) && !type.equals(Object.class);
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
