package io.spbx.webby.routekit;

import io.spbx.util.base.CharArray;
import io.spbx.util.base.MutableCharArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class RouterTest {
    // Trivial cases

    @Test
    public void routeOrNull_empty() {
        Router<String> router = new RouterSetup<String>().build();
        assertRoute(router.routeOrNull("")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("a")).is404();
        assertRoute(router.routeOrNull("foo")).is404();
    }

    @Test
    public void routeOrNull_const_rules_no_prefix() {
        Router<String> router = new RouterSetup<String>()
            .add("foo", "1")
            .add("bar", "2")
            .build();

        assertRoute(router.routeOrNull("foo")).isOk("1");
        assertRoute(router.routeOrNull("bar")).isOk("2");

        assertRoute(router.routeOrNull("")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("/foo")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();
    }

    @Test
    public void routeOrNull_const_rules_common_prefix() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/foo", "1")
            .add("/foo/bar", "2")
            .build();

        assertRoute(router.routeOrNull("/foo/foo")).isOk("1");
        assertRoute(router.routeOrNull("/foo/bar")).isOk("2");

        assertRoute(router.routeOrNull("")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("/foo")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/foo/foo/")).is404();
        assertRoute(router.routeOrNull("/foo/bar/")).is404();
        assertRoute(router.routeOrNull("/foo/foobar")).is404();
    }

    @Test
    public void routeOrNull_just_variable_rule() {
        Router<String> router = new RouterSetup<String>()
            .add("{var}", "1")
            .build();

        assertRoute(router.routeOrNull("foo")).isOk("1", "var=foo");
        assertRoute(router.routeOrNull("FOO")).isOk("1", "var=FOO");
        assertRoute(router.routeOrNull("_")).isOk("1", "var=_");
        assertRoute(router.routeOrNull("foo bar")).isOk("1", "var=foo bar");       // whitespace
        assertRoute(router.routeOrNull("foo\n\tbar")).isOk("1", "var=foo\n\tbar"); // whitespace
        assertRoute(router.routeOrNull("foo%20bar")).isOk("1", "var=foo%20bar");   // escaped " "
        assertRoute(router.routeOrNull("foo%2Fbar")).isOk("1", "var=foo%2Fbar");   // escaped "/"

        // Doesn't match the slash
        assertRoute(router.routeOrNull("")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();
        assertRoute(router.routeOrNull("/foo/")).is404();
    }

    @Test
    public void routeOrNull_just_wildcard_rule() {
        Router<String> router = new RouterSetup<String>()
            .add("{*var}", "1")
            .build();

        assertRoute(router.routeOrNull("foo")).isOk("1", "var=foo");
        assertRoute(router.routeOrNull("FOO")).isOk("1", "var=FOO");
        assertRoute(router.routeOrNull("foo/")).isOk("1", "var=foo/");
        assertRoute(router.routeOrNull("foo/bar")).isOk("1", "var=foo/bar");
        assertRoute(router.routeOrNull("/")).isOk("1", "var=/");
        assertRoute(router.routeOrNull("/foo")).isOk("1", "var=/foo");
        assertRoute(router.routeOrNull("_")).isOk("1", "var=_");

        assertRoute(router.routeOrNull("")).is404();
    }

    @Test
    public void routeOrNull_const_variable_rule() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/{var}", "1")
            .build();

        assertRoute(router.routeOrNull("/foo/foo")).isOk("1", "var=foo");
        assertRoute(router.routeOrNull("/foo/FOO")).isOk("1", "var=FOO");
        assertRoute(router.routeOrNull("/foo/_")).isOk("1", "var=_");

        // Not found
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();
        assertRoute(router.routeOrNull("/foobar")).is404();

        // Doesn't match the slash
        assertRoute(router.routeOrNull("foo//")).is404();
        assertRoute(router.routeOrNull("foo/bar/")).is404();
        assertRoute(router.routeOrNull("foo/bar//")).is404();
    }

    @Test
    public void routeOrNull_const_variable_const_rule() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/{var}/bar", "1")
            .build();

        assertRoute(router.routeOrNull("/foo/foo/bar")).isOk("1", "var=foo");
        assertRoute(router.routeOrNull("/foo/FOO/bar")).isOk("1", "var=FOO");
        assertRoute(router.routeOrNull("/foo/_/bar")).isOk("1", "var=_");

        // Not found
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("foo//")).is404();
        assertRoute(router.routeOrNull("/foo/foo/")).is404();
        assertRoute(router.routeOrNull("/foo/foo/baz")).is404();
        assertRoute(router.routeOrNull("/foo//bar")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();
        assertRoute(router.routeOrNull("/foobar")).is404();
    }

    @Test
    public void routeOrNull_variable_const_rule() {
        Router<String> router = new RouterSetup<String>()
            .add("{var}/foo/", "1")
            .build();

        assertRoute(router.routeOrNull("foo/foo/")).isOk("1", "var=foo");
        assertRoute(router.routeOrNull("FOO/foo/")).isOk("1", "var=FOO");
        assertRoute(router.routeOrNull("_/foo/")).isOk("1", "var=_");

        // Not found
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("foo")).is404();
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();
        assertRoute(router.routeOrNull("foo/foo")).is404();
        assertRoute(router.routeOrNull("foo/foo/bar")).is404();
        assertRoute(router.routeOrNull("/foo")).is404();
        assertRoute(router.routeOrNull("/foo/foo")).is404();
        assertRoute(router.routeOrNull("/foo/foo/")).is404();
        assertRoute(router.routeOrNull("/foobar")).is404();

        // Doesn't match the slash
        assertRoute(router.routeOrNull("//foo/")).is404();
        assertRoute(router.routeOrNull("///foo/")).is404();
    }

    @Test
    public void routeOrNull_const_wildcard_rule() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/{*var}", "1")
            .build();

        assertRoute(router.routeOrNull("/foo/foo")).isOk("1", "var=foo");
        assertRoute(router.routeOrNull("/foo/FOO")).isOk("1", "var=FOO");
        assertRoute(router.routeOrNull("/foo/bar/")).isOk("1", "var=bar/");
        assertRoute(router.routeOrNull("/foo/bar//")).isOk("1", "var=bar//");
        assertRoute(router.routeOrNull("/foo/bar/baz")).isOk("1", "var=bar/baz");
        assertRoute(router.routeOrNull("/foo//")).isOk("1", "var=/");
        assertRoute(router.routeOrNull("/foo/_")).isOk("1", "var=_");

        // Not found
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();
        assertRoute(router.routeOrNull("foo/bar")).is404();
        assertRoute(router.routeOrNull("foo/bar/")).is404();
        assertRoute(router.routeOrNull("/foobar")).is404();
    }

    // Multiple rules

    @Test
    public void routeOrNull_variables_without_separator_invalid() {
        Assertions.assertThrows(QueryParseException.class, () ->
            new RouterSetup<String>()
                .add("/{x}", "1")
                .add("/{x}{y}", "2")  // `y` is unreachable
                .build()
        );
    }

    @Test
    public void routeOrNull_variables_separated_by_dash_invalid() {
        Assertions.assertThrows(QueryParseException.class, () ->
            new RouterSetup<String>()
                .add("/{x}", "1")
                .add("/{x}-{y}", "2")  // `y` is unreachable (recommended workaround: change variable separator).
                .build()
        );
    }

    @Test
    public void routeOrNull_variables_separated_by_slash_and_dash() {
        Router<String> router = new RouterSetup<String>()
            .add("/{x}", "1")
            .add("/{x}/-{y}", "2")
            .build();

        assertRoute(router.routeOrNull("/foo")).isOk("1", "x=foo");
        assertRoute(router.routeOrNull("/foobar")).isOk("1", "x=foobar");
        assertRoute(router.routeOrNull("/foo-bar")).isOk("1", "x=foo-bar");
        assertRoute(router.routeOrNull("/foo/-bar")).isOk("2", "x=foo", "y=bar");

        // Not found
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/foo//")).is404();
        assertRoute(router.routeOrNull("/foo/bar")).is404();
    }

    @Test
    public void routeOrNull_variables_with_same_prefix_invalid() {
        Assertions.assertThrows(RouteException.class, () ->
            new RouterSetup<String>()
                .add("/foo/{x}", "1")
                .add("/foo/{y}", "2")  // `y` is unreachable
                .build()
        );
    }

    @Test
    public void routeOrNull_variables_with_same_part_unreachable() {
        Router<String> router = new RouterSetup<String>()
            .add("/user/{name}", "1")
            .add("/user/id{id}", "2")  // unreachable (workaround: add a separator)
            .build();

        assertRoute(router.routeOrNull("/user/foo")).isOk("1", "name=foo");
        assertRoute(router.routeOrNull("/user/_")).isOk("1", "name=_");
        assertRoute(router.routeOrNull("/user/id")).isOk("1", "name=id");  // the only terminal
        assertRoute(router.routeOrNull("/user/id3")).isOk("1", "name=id3");  // longest match selected

        // Not found
        assertRoute(router.routeOrNull("/user/")).is404();
        assertRoute(router.routeOrNull("/user/foo/")).is404();
        assertRoute(router.routeOrNull("/user/id/")).is404();
    }

    @Test
    public void routeOrNull_variables_with_same_part_swapped_unreachable() {
        Router<String> router = new RouterSetup<String>()
            .add("/user/id{id}", "2")  // unreachable (workaround: add a separator)
            .add("/user/{name}", "1")
            .build();

        assertRoute(router.routeOrNull("/user/foo")).isOk("1", "name=foo");
        assertRoute(router.routeOrNull("/user/_")).isOk("1", "name=_");
        assertRoute(router.routeOrNull("/user/id3")).isOk("1", "name=id3");  // longest match selected

        // Not found
        assertRoute(router.routeOrNull("/user/")).is404();
        assertRoute(router.routeOrNull("/user/foo/")).is404();
        assertRoute(router.routeOrNull("/user/id")).is404();
        assertRoute(router.routeOrNull("/user/id/")).is404();
    }

    @Test
    public void routeOrNull_variables_with_different_parts() {
        Router<String> router = new RouterSetup<String>()
            .add("/user/{name}", "1")
            .add("/user/id/{id}", "2")
            .build();

        assertRoute(router.routeOrNull("/user/foo")).isOk("1", "name=foo");
        assertRoute(router.routeOrNull("/user/_")).isOk("1", "name=_");
        assertRoute(router.routeOrNull("/user/id")).isOk("1", "name=id");
        assertRoute(router.routeOrNull("/user/id/3")).isOk("2", "id=3");

        // Not found
        assertRoute(router.routeOrNull("/user/")).is404();
        assertRoute(router.routeOrNull("/user/foo/")).is404();
        assertRoute(router.routeOrNull("/user/id/")).is404();
        assertRoute(router.routeOrNull("/user/id//")).is404();
    }

    @Test
    public void routeOrNull_optional_variables_defined_as_hierarchy_simple() {
        Router<String> router = new RouterSetup<String>()
            .add("/{first}/", "1")
            .add("/{first}/{last}/", "2")
            .add("/{first}/{last}/{age}", "3")
            .build();

        assertRoute(router.routeOrNull("/foo bar/")).isOk("1", "first=foo bar");
        assertRoute(router.routeOrNull("/foo%20bar/")).isOk("1", "first=foo%20bar");
        assertRoute(router.routeOrNull("/Foo%20Bar/")).isOk("1", "first=Foo%20Bar");
        assertRoute(router.routeOrNull("/foo/bar/")).isOk("2", "first=foo", "last=bar");
        assertRoute(router.routeOrNull("/FOO/BAR/")).isOk("2", "first=FOO", "last=BAR");
        assertRoute(router.routeOrNull("/foo/bar/1")).isOk("3", "first=foo", "last=bar", "age=1");
        assertRoute(router.routeOrNull("/foo/bar/25")).isOk("3", "first=foo", "last=bar", "age=25");
        assertRoute(router.routeOrNull("/foo/bar/baz")).isOk("3", "first=foo", "last=bar", "age=baz");

        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("//foo")).is404();
        assertRoute(router.routeOrNull("///")).is404();
        assertRoute(router.routeOrNull("//foo/25")).is404();
        assertRoute(router.routeOrNull("/foo//25")).is404();
        assertRoute(router.routeOrNull("foo/bar/baz/25")).is404();
    }

    @Test
    public void routeOrNull_optional_variables_defined_as_hierarchy() {
        Router<String> router = new RouterSetup<String>()
            .add("/post/{id}", "1")
            .add("/post/{id}/", "2")
            .add("/post/{id}/{slug}", "3")
            .add("/post/{id}/{slug}/{ref}", "4")
            .build();

        assertRoute(router.routeOrNull("/post/1")).isOk("1", "id=1");
        assertRoute(router.routeOrNull("/post/1/")).isOk("2", "id=1");
        assertRoute(router.routeOrNull("/post/1/title")).isOk("3", "id=1", "slug=title");
        assertRoute(router.routeOrNull("/post/1/title/42")).isOk("4", "id=1", "slug=title", "ref=42");

        // Not found
        assertRoute(router.routeOrNull("/post/")).is404();
        assertRoute(router.routeOrNull("/post//")).is404();
        assertRoute(router.routeOrNull("/post///")).is404();
    }

    @Test
    public void routeOrNull_optional_variables_defined_as_hierarchy_with_defaults() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/bar", "1")
            .add("/foo/{name}", "2")
            .add("/foo/{name}/{age}", "3")
            .build();

        // Const
        assertRoute(router.routeOrNull("/foo/bar")).isOk("1");
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("/foo")).is404();
        assertRoute(router.routeOrNull("/foobar")).is404();
        assertRoute(router.routeOrNull("/foo/bar/")).is404();

        // First var
        assertRoute(router.routeOrNull("/foo/foo")).isOk("2", "name=foo");
        assertRoute(router.routeOrNull("/foo/XXX")).isOk("2", "name=XXX");
        assertRoute(router.routeOrNull("/foo/_")).isOk("2", "name=_");
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/foo/XXX/")).is404();

        // Second var
        assertRoute(router.routeOrNull("/foo/XXX/25")).isOk("3", "name=XXX", "age=25");
        assertRoute(router.routeOrNull("/foo/XXX/25/")).is404();
        assertRoute(router.routeOrNull("/foo/XXX//")).is404();
    }

    @Test
    public void routeOrNull_just_const_and_just_variable_rules() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo", "1")
            .add("/{var}", "2")
            .build();

        assertRoute(router.routeOrNull("/foo")).isOk("1");
        assertRoute(router.routeOrNull("/bar")).isOk("2", "var=bar");
        assertRoute(router.routeOrNull("/foobar")).isOk("2", "var=foobar");

        // Not found
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("foo")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();

        // Doesn't match the slash
        assertRoute(router.routeOrNull("/foo/")).is404();
    }

    @Test
    public void routeOrNull_just_const_and_just_variable_rules_swapped() {
        Router<String> router = new RouterSetup<String>()
            .add("/{var}", "2")
            .add("/foo", "1")
            .build();

        assertRoute(router.routeOrNull("/foo")).isOk("1");
        assertRoute(router.routeOrNull("/bar")).isOk("2", "var=bar");
        assertRoute(router.routeOrNull("/foobar")).isOk("2", "var=foobar");

        // Not found
        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/")).is404();
        assertRoute(router.routeOrNull("//")).is404();
        assertRoute(router.routeOrNull("foo")).is404();
        assertRoute(router.routeOrNull("foo/")).is404();

        // Doesn't match the slash
        assertRoute(router.routeOrNull("/foo/")).is404();
    }

    @Test
    public void routeOrNull_two_rules_variable_and_const_default() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/{name}/default", "1")
            .add("/foo/{name}/{age}", "2")
            .build();

        assertRoute(router.routeOrNull("/foo/bar/1")).isOk("2", "name=bar", "age=1");
        assertRoute(router.routeOrNull("/foo/bar/25")).isOk("2", "name=bar", "age=25");
        assertRoute(router.routeOrNull("/foo/bar/def")).isOk("2", "name=bar", "age=def");
        assertRoute(router.routeOrNull("/foo/bar/default")).isOk("1", "name=bar");
        assertRoute(router.routeOrNull("/foo/bar/default25")).isOk("2", "name=bar", "age=default25");
    }

    @Test
    public void routeOrNull_two_rules_variable_and_const_default_swapped() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/{name}/{age}", "2")
            .add("/foo/{name}/default", "1")
            .build();

        assertRoute(router.routeOrNull("/foo/bar/1")).isOk("2", "name=bar", "age=1");
        assertRoute(router.routeOrNull("/foo/bar/25")).isOk("2", "name=bar", "age=25");
        assertRoute(router.routeOrNull("/foo/bar/def")).isOk("2", "name=bar", "age=def");
        // assertRoute(router.routeOrNull("/foo/bar/default")).isOk("1", "name=bar");  // TODO: fix with priorities
        assertRoute(router.routeOrNull("/foo/bar/default25")).isOk("2", "name=bar", "age=default25");

        assertRoute(router.routeOrNull("/foo/")).is404();
        assertRoute(router.routeOrNull("/foo//")).is404();
        assertRoute(router.routeOrNull("/foo///")).is404();
    }

    @Test
    public void routeOrNull_three_rules_two_vars_and_wildcard_all_matching() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/{name}/default", "1")
            .add("/foo/{name}/{age}", "2")
            .add("/foo/{name}/{*rest}", "3")
            .build();

        assertRoute(router.routeOrNull("/foo/bar/1")).isOk("2", "name=bar", "age=1");
        assertRoute(router.routeOrNull("/foo/bar/25")).isOk("2", "name=bar", "age=25");
        assertRoute(router.routeOrNull("/foo/bar/def")).isOk("2", "name=bar", "age=def");
        assertRoute(router.routeOrNull("/foo/bar/default")).isOk("1", "name=bar");
        assertRoute(router.routeOrNull("/foo/bar/default25")).isOk("2", "name=bar", "age=default25");
        assertRoute(router.routeOrNull("/foo/bar/default/")).isOk("3", "name=bar", "rest=default/");
        assertRoute(router.routeOrNull("/foo/bar/default25/")).isOk("3", "name=bar", "rest=default25/");
        assertRoute(router.routeOrNull("/foo/bar/default/25")).isOk("3", "name=bar", "rest=default/25");
    }

    @Test
    public void routeOrNull_char_buffer_as_input() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/{name}", "1")
            .build();

        assertRoute(router.routeOrNull(new CharArray("/bar/foo/name"))).is404();
        assertRoute(router.routeOrNull(new CharArray("/bar/foo/name").substringFrom(4))).isOk("1", "name=name");
        assertRoute(router.routeOrNull(new CharArray("/foo/name?k=v"))).isOk("1", Collections.singletonMap("name", "name?k=v"));
        assertRoute(router.routeOrNull(new CharArray("/foo/name?k=v").substringUntil(9))).isOk("1", "name=name");
    }

    @Test
    public void routeOrNull_quick_match_char_buffer_as_input() {
        Router<String> router = new RouterSetup<String>()
            .add("/foo/bar", "1")
            .add("/foo/{name}", "2")
            .build();

        assertRoute(router.routeOrNull("/foo/bar")).isOk("1");
        assertRoute(router.routeOrNull(new CharArray("/foo/bar"))).isOk("1");
        assertRoute(router.routeOrNull(new CharArray("/foo/bar/", 0, 8))).isOk("1");
        assertRoute(router.routeOrNull(new CharArray("//foo/bar/", 1, 9))).isOk("1");
        assertRoute(router.routeOrNull(new MutableCharArray("//foo/bar/", 1, 9))).isOk("1");

        assertRoute(router.routeOrNull(new CharArray("//foo/bar/", 0, 9))).is404();
    }

    private static @NotNull RouteSubject assertRoute(@Nullable Match<String> match) {
        return new RouteSubject(match);
    }

    private record RouteSubject(@Nullable Match<String> match) {
        public void isOk(String tag, String... variables) {
            assertThat(match).isEqualTo(match(tag, variables));
        }

        public void isOk(String tag, Map<String, String> variables) {
            assertThat(match).isEqualTo(match(tag, variables));
        }

        public void is404() {
            assertThat(match).isNull();
        }

        private static Match<String> match(String tag, String ... variables) {
            Map<String, CharArray> map = Arrays.stream(variables)
                .map(var -> var.split("=", -1))
                .filter(split -> split.length == 2)
                .collect(Collectors.toMap(
                    split -> split[0],
                    split -> new CharArray(split[1]),
                    (val1, val2) -> {throw new IllegalStateException("Duplicate values: " + val1 + " " + val2);},
                    LinkedHashMap::new)
                );
            return makeMatch(tag, map);
        }

        private static <T extends CharSequence> Match<String> match(String tag, Map<String, T> variables) {
            Map<String, CharArray> buffers = variables.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new CharArray(e.getValue())));
            return makeMatch(tag, buffers);
        }

        private static Match<String> makeMatch(String tag, Map<String, CharArray> variables) {
            return new Match<>(tag, variables);
        }
    }
}
