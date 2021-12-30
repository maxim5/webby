package io.webby.url.impl;

import io.webby.url.annotate.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.webby.url.impl.HandlerScanner.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandlerScannerTest {
    @Test
    public void matchesAny_class_one_annotation() {
        @Json
        interface Foo {}

        assertTrue(matchesAny(Foo.class, Set.of(Json.class)));
        assertFalse(matchesAny(Foo.class, Set.of(Serve.class)));
        assertTrue(matchesAny(Foo.class, Set.of(Serve.class, Json.class)));
        assertTrue(matchesAny(Foo.class, Set.of(Protobuf.class, Json.class)));
        assertFalse(matchesAny(Foo.class, Set.of(Protobuf.class, Serve.class)));
        assertFalse(matchesAny(Foo.class, Set.of(Protobuf.class)));
    }

    @Test
    public void matchesAny_class_two_annotations() {
        @Serve @Json interface Foo {}

        assertTrue(matchesAny(Foo.class, Set.of(Json.class)));
        assertTrue(matchesAny(Foo.class, Set.of(Serve.class)));
        assertTrue(matchesAny(Foo.class, Set.of(Serve.class, Json.class)));
        assertTrue(matchesAny(Foo.class, Set.of(Protobuf.class, Serve.class)));
        assertFalse(matchesAny(Foo.class, Set.of(Protobuf.class)));
    }

    @Test
    public void isHandlerClass_serve_class_matches() {
        @Serve interface Foo {}

        assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_serve_class_get_method_matches() {
        @Serve interface Foo {
            @GET
            void foo();
        }

        assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_only_get_method_matches() {
        interface Foo {
            @GET void foo();
        }

        assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_only_post_method_matches() {
        interface Foo {
            @POST
            void foo();
        }

        assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_only_call_method_matches() {
        interface Foo {
            @Call
            void foo();
        }

        assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_simple_class_no_match() {
        assertFalse(isHandlerClassDefault(HandlerScanner.class));
        assertFalse(isHandlerClassDefault(HandlerScannerTest.class));
    }

    @Test
    public void isHandlerClass_test_method_match() {
        assertTrue(isHandlerClass(HandlerScannerTest.class, Serve.class, Set.of(), Set.of(Test.class)));
    }

    private static boolean isHandlerClassDefault(@NotNull Class<?> klass) {
        return isHandlerClass(klass, MAIN_HANDLER_ANNOTATION, HANDLER_CLASS_ANNOTATIONS, HANDLER_METHOD_ANNOTATIONS);
    }
}
