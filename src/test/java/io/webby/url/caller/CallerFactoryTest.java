package io.webby.url.caller;

import com.google.common.collect.Streams;
import com.google.inject.Injector;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;
import io.routekit.util.CharArray;
import io.routekit.util.MutableCharArray;
import io.webby.netty.request.HttpRequestEx;
import io.webby.testing.Testing;
import io.webby.url.HandlerConfigError;
import io.webby.url.convert.ConversionError;
import io.webby.url.handle.Handler;
import io.webby.url.handle.IntHandler;
import io.webby.url.handle.StringHandler;
import io.webby.url.impl.Binding;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongFunction;

import static io.webby.testing.FakeRequests.getEx;
import static io.webby.testing.FakeRequests.postEx;
import static io.webby.util.collect.EasyMaps.asMap;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public class CallerFactoryTest {
    private static final String URL = "/dummy/CallerFactoryTest";
    private final CallerFactory factory = Testing.testStartup().getInstance(CallerFactory.class);

    @Test
    public void types() {
        assertTrue(CallerFactory.isInt(int.class));
        assertFalse(CallerFactory.isInt(short.class));
        assertFalse(CallerFactory.isInt(long.class));
        assertFalse(CallerFactory.isInt(byte.class));
        assertFalse(CallerFactory.isInt(char.class));
        assertFalse(CallerFactory.isInt(Integer.class));
        assertFalse(CallerFactory.isInt(Number.class));

        assertTrue(CallerFactory.isStringLike(String.class));
        assertTrue(CallerFactory.isStringLike(CharSequence.class));
        assertTrue(CallerFactory.isStringLike(CharArray.class));
        assertTrue(CallerFactory.isStringLike(MutableCharArray.class));
        assertTrue(CallerFactory.isStringLike(AsciiString.class));
        assertTrue(CallerFactory.isStringLike(StringBuilder.class));
        assertFalse(CallerFactory.isStringLike(Object.class));

        assertTrue(CallerFactory.canPassHttpRequest(HttpRequest.class));
        assertTrue(CallerFactory.canPassHttpRequest(FullHttpRequest.class));
        assertTrue(CallerFactory.canPassHttpRequest(DefaultHttpRequest.class));
        assertTrue(CallerFactory.canPassHttpRequest(DefaultFullHttpRequest.class));
        assertTrue(CallerFactory.canPassHttpRequest(HttpRequestEx.class));
        assertFalse(CallerFactory.canPassHttpRequest(Object.class));

        assertTrue(CallerFactory.canPassContent(Object.class));
        assertTrue(CallerFactory.canPassContent(Map.class));
        assertFalse(CallerFactory.canPassContent(int.class));
        assertFalse(CallerFactory.canPassContent(String.class));
        assertFalse(CallerFactory.canPassContent(CharSequence.class));

        assertTrue(CallerFactory.canPassBuffer(CharSequence.class));
        assertTrue(CallerFactory.canPassBuffer(CharArray.class));
        assertFalse(CallerFactory.canPassBuffer(MutableCharArray.class));  // routekit contains immutable
        assertFalse(CallerFactory.canPassBuffer(String.class));
        assertFalse(CallerFactory.canPassBuffer(StringBuilder.class));
        assertFalse(CallerFactory.canPassBuffer(Object.class));
    }

    @Test
    public void native_handler() throws Exception {
        Handler<String> instance = (request, variables) -> "%s:%s".formatted(request.method(), variables.keySet());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("foo", "bar"));

        assertTrue(caller instanceof NativeCaller);
        assertEquals("GET:[foo]", caller.call(get(), vars("foo", 1)));
        assertEquals("POST:[bar]", caller.call(post(), vars("bar", "x")));
        assertEquals("GET:[foo, bar]", caller.call(get(), vars("foo", 1, "bar", 2)));
    }

    @Test
    public void native_int_handler() throws Exception {
        IntHandler<String> instance = (request, val) -> "%s:%d".formatted(request.method(), val);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("foo"));

        assertTrue(caller instanceof NativeIntCaller);
        assertEquals("GET:1", caller.call(get(), vars("foo", 1)));
        assertEquals("POST:-1", caller.call(post(), vars("foo", -1)));
        assertThrows(ConversionError.class, () -> caller.call(post(), vars("bar", 1)));
        assertThrows(ConversionError.class, () -> caller.call(post(), vars("foo", "x")));
    }

    @Test
    public void native_string_handler() throws Exception {
        StringHandler<String> instance = (request, val) -> "%s:%s".formatted(request.method(), val);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("foo"));

        assertTrue(caller instanceof NativeStringCaller);
        assertEquals("GET:1", caller.call(get(), vars("foo", "1")));
        assertEquals("POST:bar", caller.call(post(), vars("foo", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(post(), vars("bar", "foo")));
    }

    @Test
    public void optimized_zero_vars() throws Exception {
        IntSupplier instance = () -> 42;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertEquals(42, caller.call(get(), vars()));
        assertEquals(42, caller.call(post(), vars()));
    }

    @Test
    public void optimized_zero_vars_with_request() throws Exception {
        interface RequestFunction {
            String apply(HttpRequest request);
        }
        RequestFunction instance = (request) -> request.method().toString();
        Caller caller = factory.create(instance, binding(instance, "POST"), Map.of(), List.of());

        assertEquals("POST", caller.call(post(), vars()));
        // Note: does not fail: RouteEndpoint is responsible for cutting off wrong requests.
        assertEquals("GET", caller.call(get(), vars()));
    }

    @Test
    public void optimized_one_var_int() throws Exception {
        IntFunction<String> instance = String::valueOf;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("10", caller.call(get(), vars("i", 10)));
        assertEquals("10", caller.call(post(), vars("i", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void optimized_one_var_int_with_request() throws Exception {
        interface IntRequestFunction {
            String apply(HttpRequest request, int i);
        }
        IntRequestFunction instance = (request, i) -> "%s:%s".formatted(request.method(), i);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("GET:10", caller.call(get(), vars("i", 10)));
        assertEquals("GET:0", caller.call(get(), vars("i", 0)));
        assertEquals("POST:10", caller.call(post(), vars("i", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void optimized_one_var_string() throws Exception {
        interface StringFunction {
            String apply(String s);
        }
        StringFunction instance = String::toUpperCase;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("str"));

        assertEquals("FOO", caller.call(get(), vars("str", "foo")));
        assertEquals("BAR", caller.call(post(), vars("str", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_one_var_char_array() throws Exception {
        interface StringFunction {
            int apply(CharArray s);
        }
        StringFunction instance = CharArray::length;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("str"));

        assertEquals(3, caller.call(get(), vars("str", "foo")));
        assertEquals(3, caller.call(post(), vars("str", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_two_vars_int_int() throws Exception {
        interface BiIntFunction {
            String apply(int x, int y);
        }
        BiIntFunction instance = "%d:%d"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i", "j"));

        assertEquals("1:0", caller.call(get(), vars("i", 1, "j", 0)));
        assertEquals("-1:-1", caller.call(post(), vars("i", -1, "j", -1)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("j", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", 10, "j", "foo")));
    }

    @Test
    public void optimized_two_vars_string_string() throws Exception {
        interface BiStrFunction {
            String apply(String a, String b);
        }
        BiStrFunction instance = "%s:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("a", "b"));

        assertEquals("foo:bar", caller.call(get(), vars("a", "foo", "b", "bar")));
        assertEquals("foo:", caller.call(get(), vars("a", "foo", "b", "")));
        assertEquals(":bar", caller.call(post(), vars("a", "", "b", "bar")));
        assertEquals(":", caller.call(post(), vars("a", "", "b", "")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("a", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("b", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_two_vars_int_string() throws Exception {
        interface IntStrFunction {
            String apply(int a, String b);
        }
        IntStrFunction instance = "%d:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("a", "b"));

        assertEquals("1:bar", caller.call(get(), vars("a", 1, "b", "bar")));
        assertEquals("0:", caller.call(get(), vars("a", 0, "b", "")));
        assertEquals("0:bar", caller.call(post(), vars("a", 0, "b", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("a", "foo", "b", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("a", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("b", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_unrecognized_one_var_object() {
        Function<Object, String> instance = Object::toString;
        assertThrows(HandlerConfigError.class, () -> factory.create(instance, binding(instance), Map.of(), List.of("x")));
    }

    @Test
    public void generic_zero_vars_injected_deps() throws Exception {
        interface InjectedFunction {
            String apply(Injector injector);
        }
        InjectedFunction instance = (injector) -> "%s".formatted(injector.getClass().getName());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertEquals("com.google.inject.internal.InjectorImpl", caller.call(get(), vars()));
    }

    @Test
    public void generic_zero_vars_injected_deps_with_request() throws Exception {
        interface InjectedFunction {
            String apply(Injector injector, HttpRequest request);
        }
        InjectedFunction instance = (injector, request) -> "%s:%s".formatted(injector.getClass().getName(), request.method());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertEquals("com.google.inject.internal.InjectorImpl:GET", caller.call(get(), vars()));
    }

    @Test
    public void generic_one_var_long() throws Exception {
        LongFunction<String> instance = String::valueOf;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("9223372036854775807", caller.call(get(), vars("i", Long.MAX_VALUE)));
        assertEquals("-9223372036854775808", caller.call(post(), vars("i", Long.MIN_VALUE)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void generic_one_var_injected_deps() throws Exception {
        interface IntInjectedFunction {
            String apply(Injector injector, int i);
        }
        IntInjectedFunction instance = (injector, i) -> "%s:%d".formatted(injector.getClass().getName(), i);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("com.google.inject.internal.InjectorImpl:10", caller.call(get(), vars("i", 10)));
    }

    @Test
    public void generic_one_var_injected_deps_swapped() throws Exception {
        interface IntInjectedFunction {
            String apply(int i, Injector injector);
        }
        IntInjectedFunction instance = (i, injector) -> "%d:%s".formatted(i, injector.getClass().getName());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("10:com.google.inject.internal.InjectorImpl", caller.call(get(), vars("i", 10)));
    }

    @Test
    public void generic_many_vars_primitive_with_request() throws Exception {
        interface GenericFunction {
            String apply(HttpRequest request, int x, long y, byte z, String s);
        }
        GenericFunction instance = (request, x, y, z, s) -> "%s:%d:%d:%d:%s".formatted(request.method(), x, y, z, s);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z", "s"));

        assertEquals("GET:1:2:3:foobar", caller.call(get(), vars("x", 1, "y", 2, "z", 3, "s", "foobar")));
        assertEquals("GET:0:9223372036854775807:127:foo",
                     caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127, "s", "foo")));
        assertEquals("GET:-10:-9223372036854775808:-128:bar",
                     caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128, "s", "bar")));

        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "z", 256, "s", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "z", 3, "str", "foo")));
    }

    @Test
    public void generic_many_vars_boxed() throws Exception {
        interface GenericFunction {
            String apply(Integer x, Long y, Byte z);
        }
        GenericFunction instance = "%d:%d:%d"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z"));

        assertEquals("1:2:3",caller.call(get(), vars("x", 1, "y", 2, "z", 3)));
        assertEquals("0:9223372036854775807:127", caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127)));
        assertEquals("-10:-9223372036854775808:-128", caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128)));

        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "z", 256)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "w", 3)));
    }

    @Test
    public void generic_many_vars_primitive_and_boxed() throws Exception {
        interface GenericFunction {
            String apply(short x, float y, Double z, boolean b);
        }
        GenericFunction instance = "%d:%.2f:%.2f:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z", "b"));

        assertEquals("1:2.00:3.00:false", caller.call(get(), vars("x", 1, "y", 2.0, "z", 3.0, "b", false)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1_000_000, "y", 0, "z", 0, "b", true)));
    }

    @Test
    public void generic_two_vars_primitive_and_boxed_character() throws Exception {
        interface GenericFunction {
            String apply(char ch1, Character ch2);
        }
        GenericFunction instance = "%s:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y"));

        assertEquals("A:B", caller.call(get(), vars("x", 65, "y", 66)));
        assertEquals("\u90AB:\u0000", caller.call(get(), vars("x", 0x90AB, "y", 0)));
        // assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", -1, "y", -2)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", -1, "y", Long.MAX_VALUE)));
    }

    private static @NotNull Binding binding(Object instance) {
        return binding(instance, null);
    }

    private static @NotNull Binding binding(Object instance, String type) {
        return new Binding(URL, getSingleDeclaredMethod(instance), type, EndpointOptions.DEFAULT);
    }

    private static @NotNull Method getSingleDeclaredMethod(Object instance) {
        Method[] methods = instance.getClass().getDeclaredMethods();
        assertEquals(1, methods.length);
        return methods[0];
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Map<String, CharArray> vars(Object @NotNull ... items) {
        List<CharSequence> list = Streams.mapWithIndex(
                Arrays.stream(items),
                (item, i) -> (i % 2 == 0) ? item.toString() : new CharArray(item.toString())
        ).toList();
        return asMap(list);
    }

    public static @NotNull HttpRequestEx get() {
        return getEx(URL);    // Do not care about QueryParams for now
    }

    public static @NotNull HttpRequestEx post() {
        return postEx(URL);   // Do not care about QueryParams for now
    }
}
