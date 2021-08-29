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
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import static io.webby.testing.FakeRequests.getEx;
import static io.webby.testing.FakeRequests.postEx;
import static io.webby.testing.Testing.asMap;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public class CallerFactoryTest {
    private static final String URL = "/dummy/CallerFactoryTest";
    private final CallerFactory factory = Testing.testStartupNoHandlers().getInstance(CallerFactory.class);

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
    public void zero_vars_optimized() throws Exception {
        IntSupplier instance = () -> 42;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertEquals(42, caller.call(get(), vars()));
        assertEquals(42, caller.call(post(), vars()));
    }

    @Test
    public void zero_vars_optimized_with_request() throws Exception {
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
    public void one_var_optimized_int() throws Exception {
        IntFunction<String> instance = String::valueOf;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("10", caller.call(get(), vars("i", 10)));
        assertEquals("10", caller.call(post(), vars("i", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void one_var_optimized_int_with_request() throws Exception {
        interface IntRequestFunction {
            String apply(HttpRequest request, int i);
        }
        IntRequestFunction instance = (request, i) -> "%s:%s".formatted(request.method(), i);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("GET:10", caller.call(get(), vars("i", 10)));
        assertEquals("GET:0", caller.call(get(), vars("i", 0)));
        assertEquals("POST:10", caller.call(post(), vars("i", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void one_var_optimized_string() throws Exception {
        StringFunction<String> instance = String::toUpperCase;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("str"));

        assertEquals("FOO", caller.call(get(), vars("str", "foo")));
        assertEquals("BAR", caller.call(post(), vars("str", "bar")));
    }

    @Test
    public void zero_vars_generic_injected_deps() throws Exception {
        interface InjectedFunction {
            String apply(Injector injector);
        }
        InjectedFunction instance = (injector) -> "%s".formatted(injector.getClass().getName());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertEquals("com.google.inject.internal.InjectorImpl", caller.call(get(), vars()));
    }

    @Test
    public void one_var_generic_injected_deps() throws Exception {
        interface IntInjectedFunction {
            String apply(Injector injector, int i);
        }
        IntInjectedFunction instance = (injector, i) -> "%s:%d".formatted(injector.getClass().getName(), i);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertEquals("com.google.inject.internal.InjectorImpl:10", caller.call(get(), vars("i", 10)));
    }

    @Test
    public void many_vars_generic_primitive_with_request() throws Exception {
        interface GenericFunction {
            String apply(HttpRequest request, int x, long y, byte z, String s);
        }
        GenericFunction instance = (request, x, y, z, s) -> "%s:%d:%d:%d:%s".formatted(request.method(), x, y, z, s);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z", "s"));

        assertEquals(
                "GET:1:2:3:foobar",
                caller.call(get(), vars("x", 1, "y", 2, "z", 3, "s", "foobar"))
        );
        assertEquals(
                "GET:0:9223372036854775807:127:foo",
                caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127, "s", "foo"))
        );
        assertEquals(
                "GET:-10:-9223372036854775808:-128:bar",
                caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128, "s", "bar"))
        );

        assertThrows(ConversionError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "z", 256, "s", "foo")));
        assertThrows(ConversionError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "z", 3, "str", "foo")));
    }

    @Test
    public void many_vars_generic_boxed() throws Exception {
        interface GenericFunction {
            String apply(Integer x, Long y, Byte z);
        }
        GenericFunction instance = "%d:%d:%d"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z"));

        assertEquals("1:2:3",caller.call(get(), vars("x", 1, "y", 2, "z", 3)));
        assertEquals("0:9223372036854775807:127", caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127)));
        assertEquals("-10:-9223372036854775808:-128", caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128)));

        assertThrows(ConversionError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "z", 256)));
        assertThrows(ConversionError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "w", 3)));
    }

    @Test
    public void many_vars_generic_primitive_and_boxed() throws Exception {
        interface GenericFunction {
            String apply(short x, float y, Double z, boolean b);
        }
        GenericFunction instance = "%d:%.2f:%.2f:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z", "b"));

        assertEquals("1:2.00:3.00:false", caller.call(get(), vars("x", 1, "y", 2.0, "z", 3.0, "b", false)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1_000_000, "y", 0, "z", 0, "b", true)));
    }

    @Test
    public void two_vars_generic_primitive_and_boxed_character() throws Exception {
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

    @NotNull
    private static Binding binding(Object instance) {
        return binding(instance, null);
    }

    @NotNull
    private static Binding binding(Object instance, String type) {
        return new Binding(URL, getDeclaredMethod(instance), type, EndpointOptions.DEFAULT);
    }

    @NotNull
    private static Method getDeclaredMethod(Object instance) {
        Method[] methods = instance.getClass().getDeclaredMethods();
        assertEquals(1, methods.length);
        return methods[0];
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Map<String, CharArray> vars(Object ... items) {
        List<CharSequence> list = Streams.mapWithIndex(
                Arrays.stream(items),
                (item, i) -> (i % 2 == 0) ? item.toString() : new CharArray(item.toString())
        ).toList();
        return asMap(list);
    }

    @NotNull
    public static HttpRequestEx get() {
        return getEx(URL);    // Do not care about QueryParams for now
    }

    @NotNull
    public static HttpRequestEx post() {
        return postEx(URL);   // Do not care about QueryParams for now
    }

    private interface StringFunction<T> {
        T apply(String s);
    }
}
