package io.webby.routekit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

public class SimpleQueryParserTest {
    @Test
    public void parse_no_variables() {
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/"), constant("/"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("//"), constant("//"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("//*"), constant("//*"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("foo/bar/baz"), constant("foo/bar/baz"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/foo/bar/baz"), constant("/foo/bar/baz"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("foo-bar-baz"), constant("foo-bar-baz"));
    }

    @Test
    public void parse_variable() {
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{foo}"), var("foo"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/{foo}"), constant("/"), var("foo"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{foo}/"), var("foo"), constant("/"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/{foo}/"), constant("/"), var("foo"), constant("/"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/foo/{foo}"), constant("/foo/"), var("foo"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("foo{foo}/foo"), constant("foo"), var("foo"), constant("/foo"));

        assertOrdered(SimpleQueryParser.DEFAULT.parse("{X}"), var("X"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{XyZ}"), var("XyZ"));
    }

    @Test
    public void parse_two_variables() {
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{foo}/{bar}"), var("foo"), constant("/"), var("bar"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/{foo}/{bar}"), constant("/"), var("foo"), constant("/"), var("bar"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/{foo}/{bar}/"), constant("/"), var("foo"), constant("/"), var("bar"), constant("/"));

        assertOrdered(SimpleQueryParser.DEFAULT.parse("{X}/{Y}"), var("X"), constant("/"), var("Y"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{X}/ {Y}"), var("X"), constant("/ "), var("Y"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{X}/_{Y}"), var("X"), constant("/_"), var("Y"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{X}/-{Y}"), var("X"), constant("/-"), var("Y"));
    }

    @Test
    public void parse_wildcard() {
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{*foo}"), wildcard("foo"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("*{*foo}"), constant("*"), wildcard("foo"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/{*foo}"), constant("/"), wildcard("foo"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/foo/{*foo}"), constant("/foo/"), wildcard("foo"));

        assertOrdered(SimpleQueryParser.DEFAULT.parse("{*X}"), wildcard("X"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{*XyZ}"), wildcard("XyZ"));
    }

    @Test
    public void parse_wildcard_and_variable() {
        assertOrdered(SimpleQueryParser.DEFAULT.parse("{foo}/{*bar}"), var("foo"), constant("/"), wildcard("bar"));
        assertOrdered(SimpleQueryParser.DEFAULT.parse("/foo/{foo}/{*bar}"), constant("/foo/"), var("foo"), constant("/"), wildcard("bar"));
    }

    @Test
    public void parse_many_variables() {
        assertOrdered(
            SimpleQueryParser.DEFAULT.parse("/{foo}/{bar}/{baz}"),
            constant("/"), var("foo"), constant("/"), var("bar"), constant("/"), var("baz")
        );
        assertOrdered(
            SimpleQueryParser.DEFAULT.parse("/foo/{bar}/{baz}/"),
            constant("/foo/"), var("bar"), constant("/"), var("baz"), constant("/")
        );
        assertOrdered(
            SimpleQueryParser.DEFAULT.parse("/foo/{xxx}/{y}/{*z}"),
            constant("/foo/"), var("xxx"), constant("/"), var("y"), constant("/"), wildcard("z")
        );
    }

    @Test
    public void parse_invalid() {
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse(""));

        // Invalid brackets
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}{"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}foo{"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}{foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}{}{"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{{}}"));  // nesting not allowed

        // Invalid name
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{**}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo*}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo*foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*foo*}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{{foo}}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo-bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo+bar}"));

        // Duplicates
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{dup}{dup}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{dup}/{dup}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{dup}{*dup}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{dup}/{*dup}"));

        // Wildcard not at the end
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}/foo"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}/{foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}{foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}{*bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/foo/{*bar}/"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/foo/{foo}/{*bar}/"));

        // Vars not separated
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo}{bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{foo}{bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo}{bar}/"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo}{*bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{foo}{*bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/foo/{foo}{*bar}"));

        // Vars not properly separated
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{X} {Y}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{X}_{Y}"));
        Assertions.assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{X}-{Y}"));
    }

    @SafeVarargs
    private static <T> void assertOrdered(Collection<T> actual, T ... expected) {
        Assertions.assertEquals(Arrays.asList(expected), actual);
    }

    private static ConstToken constant(String token) {
        return new ConstToken(token);
    }

    private static SeparableVariableToken var(String name) {
        return new SeparableVariableToken(name);
    }

    private static WildcardToken wildcard(String name) {
        return new WildcardToken(name);
    }
}
