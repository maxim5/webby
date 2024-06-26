package io.spbx.webby.routekit;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.spbx.util.testing.MoreTruth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RouterBuilderTest {
    @Test
    public void buildStateMachine_empty() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of());
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                              """);
    }

    @Test
    public void buildStateMachine_constants_common_part() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/foo/bar"),
            rule("2", "/foo/baz")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/foo/ba]
                                      ConstToken[r] -> 1
                                      ConstToken[z] -> 2
                              """);
    }

    @Test
    public void buildStateMachine_constants_no_common_part() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "foo"),
            rule("2", "bar"),
            rule("3", "baz")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[foo] -> 1
                                  ConstToken[bar] -> 2
                                  ConstToken[baz] -> 3
                              """);
    }

    @Test
    public void buildStateMachine_constants_common_prefix() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/foobar"),
            rule("2", "/foobaz")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/fooba]
                                      ConstToken[r] -> 1
                                      ConstToken[z] -> 2
                              """);
    }

    @Test
    public void buildStateMachine_constants_two_common_prefixes_in_parallel() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/foobar"),
            rule("2", "/foobaz"),
            rule("3", "/bar"),
            rule("4", "/barbaz")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/]
                                      ConstToken[foobar] -> 1
                                      ConstToken[foobaz] -> 2
                                      ConstToken[bar] -> 3
                                      ConstToken[barbaz] -> 4
                              """);
    }

    @Test
    public void buildStateMachine_constants_simple_hierarchy() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/"),
            rule("2", "/foo"),
            rule("3", "/foo/"),
            rule("4", "/foo/bar")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/] -> 1
                                      ConstToken[foo] -> 2
                                          ConstToken[/] -> 3
                                              ConstToken[bar] -> 4
                              """);
    }

    @Test
    public void buildStateMachine_constants_hierarchy_parts_joined() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/"),
            rule("2", "/foo/foo"),
            rule("3", "/foo/foo/bar/bar")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/] -> 1
                                      ConstToken[foo/foo] -> 2
                                          ConstToken[/bar/bar] -> 3
                              """);
    }

    @Test
    public void buildStateMachine_constants_common_at_second_level() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/user/foo"),
            rule("2", "/user/bar"),
            rule("3", "/usergroup/"),
            rule("4", "/usergroup/baz"),
            rule("5", "/search/foo"),
            rule("6", "/search/bar")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/]
                                      ConstToken[user/]
                                          ConstToken[foo] -> 1
                                          ConstToken[bar] -> 2
                                      ConstToken[usergroup/] -> 3
                                          ConstToken[baz] -> 4
                                      ConstToken[search/]
                                          ConstToken[foo] -> 5
                                          ConstToken[bar] -> 6
                              """);
    }

    @Test
    public void buildStateMachine_constants_hierarchy_with_sibling() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/"),
            rule("2", "/foo/bar"),
            rule("3", "/foo/bar/baz"),
            rule("4", "/bar")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/] -> 1
                                      ConstToken[foo/bar] -> 2
                                          ConstToken[/baz] -> 3
                                      ConstToken[bar] -> 4
                              """);
    }

    @Test
    public void buildStateMachine_constants_two_hierarchies() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/"),
            rule("2", "/foo/bar"),
            rule("3", "/foo/bar/baz"),
            rule("4", "/bar"),
            rule("5", "/bar/baz")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/] -> 1
                                      ConstToken[foo/bar] -> 2
                                          ConstToken[/baz] -> 3
                                      ConstToken[bar] -> 4
                                          ConstToken[/baz] -> 5
                              """);
    }

    @Test
    public void buildStateMachine_var_and_const_at_root() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "foo"),
            rule("2", "{foo}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[foo] -> 1
                                  SeparableVariableToken[foo] -> 2
                              """);
    }

    @Test
    public void buildStateMachine_var_and_const_after_const() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/", "foo"),
            rule("2", "/", "{bar}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/]
                                      ConstToken[foo] -> 1
                                      SeparableVariableToken[bar] -> 2
                              """);
    }

    @Test
    public void buildStateMachine_multiple_variables_at_root_level_not_allowed() {
        assertThrows(RouteException.class, () ->
            new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
                rule("1", "{foo}"),
                rule("2", "{bar}")
            )));
    }

    @Test
    public void buildStateMachine_multiple_variables_at_same_level_not_allowed() {
        assertThrows(RouteException.class, () ->
            new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
                rule("1", "/foo/", "{foo}"),
                rule("2", "/foo/", "{bar}")
            )));
    }

    @Test
    public void buildStateMachine_variables_hierarchy() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "{foo}"),
            rule("2", "{foo}", "{bar}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  SeparableVariableToken[foo] -> 1
                                      SeparableVariableToken[bar] -> 2
                              """);
    }

    @Test
    public void buildStateMachine_const_excluded_and_vars_with_common_prefix() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/foo/bar"),
            rule("2", "/foo/", "{name}"),
            rule("3", "/foo/", "{name}", "/", "{age}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/foo/]
                                      ConstToken[bar] -> 1
                                      SeparableVariableToken[name] -> 2
                                          ConstToken[/]
                                              SeparableVariableToken[age] -> 3
                              """);
    }

    @Test
    public void buildStateMachine_const_included_and_vars_with_common_prefix() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(true).buildStateMachine(List.of(
            rule("1", "/foo/bar"),
            rule("2", "/foo/", "{name}"),
            rule("3", "/foo/", "{name}", "/", "{age}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/foo/]
                                      SeparableVariableToken[name] -> 2
                                          ConstToken[/]
                                              SeparableVariableToken[age] -> 3
                              """);
    }

    @Test
    public void buildStateMachine_const_and_var() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/user"),
            rule("2", "/user/id", "{id}"),
            rule("2", "/user/", "{name}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/user] -> 1
                                      ConstToken[/]
                                          ConstToken[id]
                                              SeparableVariableToken[id] -> 2
                                          SeparableVariableToken[name] -> 2
                              """);
    }

    @Test
    public void buildStateMachine_const_and_var_tree() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/"),
            rule("2", "/index"),
            rule("3", "/index/", "{page}"),
            rule("4", "/user"),
            rule("5", "/user/", "{name}"),
            rule("6", "/user/id", "{id}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/] -> 1
                                      ConstToken[index] -> 2
                                          ConstToken[/]
                                              SeparableVariableToken[page] -> 3
                                      ConstToken[user] -> 4
                                          ConstToken[/]
                                              SeparableVariableToken[name] -> 5
                                              ConstToken[id]
                                                  SeparableVariableToken[id] -> 6
                              """);
    }

    @Test
    public void buildStateMachine_vars_common_prefix() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/foo/", "{name}"),
            rule("2", "/foo/", "{name}", "/", "{age}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/foo/]
                                      SeparableVariableToken[name] -> 1
                                          ConstToken[/]
                                              SeparableVariableToken[age] -> 2
                              """);
    }

    @Test
    public void buildStateMachine_var_const_and_wildcard_on_same_level() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/foo/", "{name}", "/default"),
            rule("2", "/foo/", "{name}", "/", "{age}"),
            rule("3", "/foo/", "{name}", "/", "{*rest}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/foo/]
                                      SeparableVariableToken[name]
                                          ConstToken[/]
                                              ConstToken[default] -> 1
                                              SeparableVariableToken[age] -> 2
                                              WildcardToken[rest] -> 3
                              """);
    }

    @Test
    public void buildStateMachine_optional_trailing_wildcard() {
        Router.Node<String> node = new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
            rule("1", "/foo"),
            rule("2", "/foo/", "{name}"),
            rule("3", "/foo/", "{name}", "/", "{*rest}")
        ));
        assertThatNode(node)
            .matchesStructure("""
                              <root>
                                  ConstToken[/foo] -> 1
                                      ConstToken[/]
                                          SeparableVariableToken[name] -> 2
                                              ConstToken[/]
                                                  WildcardToken[rest] -> 3
                              """);
    }

    @Test
    public void buildStateMachine_constants_duplicates() {
        assertThrows(RouteException.class,
                     () -> new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
                         rule("1", "/foo"),
                         rule("2", "/foo")
                     )));
    }

    @Test
    public void buildStateMachine_vars_duplicates() {
        assertThrows(RouteException.class,
                     () -> new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
                         rule("1", "/", "{var}"),
                         rule("2", "/", "{var}")
                     )));
    }

    @Test
    public void buildStateMachine_var_and_wildcard_duplicates() {
        assertThrows(RouteException.class,
                     () -> new RouterBuilder().setExcludeConstFromFSM(false).buildStateMachine(List.of(
                         rule("1", "/", "{var}"),
                         rule("2", "/", "{*var}")
                     )));
    }

    private static @NotNull RouterSetup.Rule<String> rule(@NotNull String tag, @NotNull String @NotNull ... tokens) {
        return new RouterSetup.Rule<>(() -> Arrays.stream(tokens).map(RouterBuilderTest::convert).toList(), tag);
    }

    private static @NotNull Token convert(@NotNull String token) {
        return token.startsWith("{") && token.endsWith("}") ?
            token.contains("*") ?
                new WildcardToken(token.replaceAll("[{}*]", "")) :
                new SeparableVariableToken(token.replaceAll("[{}*]", "")) :
            new ConstToken(token);
    }

    private static @NotNull NodeSubject assertThatNode(@NotNull Router.Node<?> node) {
        return new NodeSubject(node);
    }

    private record NodeSubject(@NotNull Router.Node<?> node) {
        public void matchesStructure(@NotNull String expected) {
            String actual = NodePrinter.printlnToString(node);
            assertThat(actual).linesMatch(expected);
        }
    }
}
