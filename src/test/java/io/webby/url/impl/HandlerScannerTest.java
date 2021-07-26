package io.webby.url.impl;

import io.webby.url.annotate.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.webby.url.impl.HandlerScanner.*;

public class HandlerScannerTest {
    @Test
    public void matchesAny_class_one_annotation() {
        @Json
        interface Foo {}

        Assertions.assertTrue(matchesAny(Foo.class, Set.of(Json.class)));
        Assertions.assertFalse(matchesAny(Foo.class, Set.of(Serve.class)));
        Assertions.assertTrue(matchesAny(Foo.class, Set.of(Serve.class, Json.class)));
        Assertions.assertTrue(matchesAny(Foo.class, Set.of(Protobuf.class, Json.class)));
        Assertions.assertFalse(matchesAny(Foo.class, Set.of(Protobuf.class, Serve.class)));
        Assertions.assertFalse(matchesAny(Foo.class, Set.of(Protobuf.class)));
    }

    @Test
    public void matchesAny_class_two_annotations() {
        @Serve @Json interface Foo {}

        Assertions.assertTrue(matchesAny(Foo.class, Set.of(Json.class)));
        Assertions.assertTrue(matchesAny(Foo.class, Set.of(Serve.class)));
        Assertions.assertTrue(matchesAny(Foo.class, Set.of(Serve.class, Json.class)));
        Assertions.assertTrue(matchesAny(Foo.class, Set.of(Protobuf.class, Serve.class)));
        Assertions.assertFalse(matchesAny(Foo.class, Set.of(Protobuf.class)));
    }

    @Test
    public void isHandlerClass_serve_class_matches() {
        @Serve interface Foo {}

        Assertions.assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_serve_class_get_method_matches() {
        @Serve interface Foo {
            @GET
            void foo();
        }

        Assertions.assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_only_get_method_matches() {
        interface Foo {
            @GET void foo();
        }

        Assertions.assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_only_post_method_matches() {
        interface Foo {
            @POST
            void foo();
        }

        Assertions.assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_only_call_method_matches() {
        interface Foo {
            @Call
            void foo();
        }

        Assertions.assertTrue(isHandlerClassDefault(Foo.class));
    }

    @Test
    public void isHandlerClass_simple_class_no_match() {
        Assertions.assertFalse(isHandlerClassDefault(HandlerScanner.class));
        Assertions.assertFalse(isHandlerClassDefault(HandlerScannerTest.class));
    }

    @Test
    public void isHandlerClass_test_method_match() {
        Assertions.assertTrue(isHandlerClass(HandlerScannerTest.class, Serve.class, Set.of(), Set.of(Test.class)));
    }

    private static boolean isHandlerClassDefault(@NotNull Class<?> klass) {
        return isHandlerClass(klass, MAIN_HANDLER_ANNOTATION, HANDLER_CLASS_ANNOTATIONS, HANDLER_METHOD_ANNOTATIONS);
    }
}
