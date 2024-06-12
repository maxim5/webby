package io.webby.routekit;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleQueryParserTest {
    @Test
    public void parse_no_variables() {
        assertThat(SimpleQueryParser.DEFAULT.parse("/")).containsExactly(constant("/")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("//")).containsExactly(constant("//")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("//*")).containsExactly(constant("//*")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("foo/bar/baz")).containsExactly(constant("foo/bar/baz")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/foo/bar/baz")).containsExactly(constant("/foo/bar/baz")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("foo-bar-baz")).containsExactly(constant("foo-bar-baz")).inOrder();
    }

    @Test
    public void parse_variable() {
        assertThat(SimpleQueryParser.DEFAULT.parse("{foo}")).containsExactly(var("foo")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/{foo}")).containsExactly(constant("/"), var("foo")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("{foo}/")).containsExactly(var("foo"), constant("/")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/{foo}/")).containsExactly(constant("/"), var("foo"), constant("/")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/foo/{foo}")).containsExactly(constant("/foo/"), var("foo")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("foo{foo}/foo")).containsExactly(constant("foo"), var("foo"), constant("/foo")).inOrder();

        assertThat(SimpleQueryParser.DEFAULT.parse("{X}")).containsExactly(var("X")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("{XyZ}")).containsExactly(var("XyZ")).inOrder();
    }

    @Test
    public void parse_two_variables() {
        assertThat(SimpleQueryParser.DEFAULT.parse("{foo}/{bar}"))
            .containsExactly(var("foo"), constant("/"), var("bar")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/{foo}/{bar}"))
            .containsExactly(constant("/"), var("foo"), constant("/"), var("bar")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/{foo}/{bar}/"))
            .containsExactly(constant("/"), var("foo"), constant("/"), var("bar"), constant("/")).inOrder();

        assertThat(SimpleQueryParser.DEFAULT.parse("{X}/{Y}")).containsExactly(var("X"), constant("/"), var("Y")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("{X}/ {Y}")).containsExactly(var("X"), constant("/ "), var("Y")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("{X}/_{Y}")).containsExactly(var("X"), constant("/_"), var("Y")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("{X}/-{Y}")).containsExactly(var("X"), constant("/-"), var("Y")).inOrder();
    }

    @Test
    public void parse_wildcard() {
        assertThat(SimpleQueryParser.DEFAULT.parse("{*foo}")).containsExactly(wildcard("foo")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("*{*foo}")).containsExactly(constant("*"), wildcard("foo")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/{*foo}")).containsExactly(constant("/"), wildcard("foo")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/foo/{*foo}")).containsExactly(constant("/foo/"), wildcard("foo")).inOrder();

        assertThat(SimpleQueryParser.DEFAULT.parse("{*X}")).containsExactly(wildcard("X")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("{*XyZ}")).containsExactly(wildcard("XyZ")).inOrder();
    }

    @Test
    public void parse_wildcard_and_variable() {
        assertThat(SimpleQueryParser.DEFAULT.parse("{foo}/{*bar}"))
            .containsExactly(var("foo"), constant("/"), wildcard("bar")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/foo/{foo}/{*bar}"))
            .containsExactly(constant("/foo/"), var("foo"), constant("/"), wildcard("bar")).inOrder();
    }

    @Test
    public void parse_many_variables() {
        assertThat(SimpleQueryParser.DEFAULT.parse("/{foo}/{bar}/{baz}"))
            .containsExactly(constant("/"), var("foo"), constant("/"), var("bar"), constant("/"), var("baz")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/foo/{bar}/{baz}/"))
            .containsExactly(constant("/foo/"), var("bar"), constant("/"), var("baz"), constant("/")).inOrder();
        assertThat(SimpleQueryParser.DEFAULT.parse("/foo/{xxx}/{y}/{*z}"))
            .containsExactly(constant("/foo/"), var("xxx"), constant("/"), var("y"), constant("/"), wildcard("z")).inOrder();
    }

    @Test
    public void parse_invalid() {
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse(""));

        // Invalid brackets
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}{"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}foo{"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}{foo}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("}{}{"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{{}}"));  // nesting not allowed

        // Invalid name
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{**}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo*}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo*foo}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*foo*}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{{foo}}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo-bar}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo+bar}"));

        // Duplicates
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{dup}{dup}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{dup}/{dup}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{dup}{*dup}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{dup}/{*dup}"));

        // Wildcard not at the end
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}/foo"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}/{foo}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}{foo}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{*bar}{*bar}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/foo/{*bar}/"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/foo/{foo}/{*bar}/"));

        // Vars not separated
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo}{bar}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{foo}{bar}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo}{bar}/"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{foo}{*bar}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/{foo}{*bar}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("/foo/{foo}{*bar}"));

        // Vars not properly separated
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{X} {Y}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{X}_{Y}"));
        assertThrows(QueryParseException.class, () -> SimpleQueryParser.DEFAULT.parse("{X}-{Y}"));
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
