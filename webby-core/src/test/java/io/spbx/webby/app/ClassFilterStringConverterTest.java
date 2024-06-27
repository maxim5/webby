package io.spbx.webby.app;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.spbx.util.base.EasyPrimitives;
import io.spbx.util.base.Pair;
import io.spbx.util.classpath.ClassNamePredicate;
import io.spbx.webby.app.ClassFilterStringConverter.PropertyParser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.AssertBasics.assertReversibleRoundtrip;

public class ClassFilterStringConverterTest {
    private static final List<String> CLASSPATH = List.of(
        "a.b.c#Main",
        "a.b.c#Main$1",
        "a.b.c#Util",
        "a.b.c.d#Aaa",
        "a.b.ab#Bbb",
        "a.bc#Ccc",
        "a#Module",
        "foo.bar#ValueTest",
        "foo.bar.nest#SmallTest",
        "foo.bar.nest#SmallTest.Nested",
        "foo.bar.nest#SmallTest.Nested$2",
        "x.y#Xy",
        "x.y.z#Util",
        "x.y.z#Util$1",
        "#TopLevel"
    );

    @Test
    public void class_filter_conversion_roundtrip() {
        assertFilterRoundtrip(ClassFilter.ALL);
        assertFilterRoundtrip(ClassFilter.NONE);
        assertFilterRoundtrip(ClassFilter.DEFAULT);
        assertFilterRoundtrip(ClassFilter.of((pkg, cls) -> pkg.startsWith("a")));
        assertFilterRoundtrip(ClassFilter.of((pkg, cls) -> pkg.startsWith("a.")));
        assertFilterRoundtrip(ClassFilter.of((pkg, cls) -> pkg.startsWith("ab.")));
        assertFilterRoundtrip(ClassFilter.of((pkg, cls) -> cls.startsWith("Util")));
        assertFilterRoundtrip(ClassFilter.of((pkg, cls) -> cls.endsWith("l")));
        assertFilterRoundtrip(ClassFilter.of((pkg, cls) -> pkg.startsWith("a.") && cls.endsWith("$1")));
        assertFilterRoundtrip(ClassFilter.of((pkg, cls) -> pkg.startsWith("a.") || cls.endsWith("$1")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1", "foo", "foobar", "01234567890123456789"})
    public void base64_roundtrip_simple(String input) {
        assertBase64Roundtrip(input, "prefix:", StandardCharsets.UTF_8);
        assertBase64Roundtrip(input, "", StandardCharsets.US_ASCII);
    }

    @Test
    public void base64_roundtrip_all_bytes() {
        byte[] bytes = IntStream.range(Byte.MIN_VALUE, Byte.MAX_VALUE + 1).boxed().collect(EasyPrimitives.toByteArray());
        assertBase64Roundtrip(bytes, ":", StandardCharsets.US_ASCII);
    }

    @Test
    public void term_defaults() {
        assertInput("all").asTerm().matchesAll();
        assertInput("none").asTerm().matchesNothing();
        assertInput("default").asTerm().matchesAll();
    }

    @Test
    public void term_simple_equals() {
        assertInput("pkg=").asTerm().matchesFiles("#TopLevel");
        assertInput("pkg=x.y").asTerm().matchesFiles("x.y#Xy");
        assertInput("cls=Xy").asTerm().matchesFiles("x.y#Xy");
        assertInput("cls=Util").asTerm().matchesFiles("a.b.c#Util", "x.y.z#Util");
    }

    @Test
    public void term_simple_starts_with() {
        assertInput("pkg~=*").asTerm().matchesAll();
        assertInput("pkg~=x.y*").asTerm().matchesFiles("x.y#Xy", "x.y.z#Util", "x.y.z#Util$1");
        assertInput("cls~=Util*").asTerm().matchesFiles("a.b.c#Util", "x.y.z#Util", "x.y.z#Util$1");
        assertInput("cls~=Main*").asTerm().matchesFiles("a.b.c#Main", "a.b.c#Main$1");
    }

    @Test
    public void term_simple_ends_with() {
        assertInput("pkg~=*y").asTerm().matchesFiles("x.y#Xy");
        assertInput("pkg~=*z").asTerm().matchesFiles("x.y.z#Util", "x.y.z#Util$1");
        assertInput("cls~=*$1").asTerm().matchesFiles("a.b.c#Main$1", "x.y.z#Util$1");
        assertInput("cls~=*l").asTerm().matchesFiles("a.b.c#Util", "x.y.z#Util", "#TopLevel");
        assertInput("cls~=*$3").asTerm().matchesNothing();
    }

    @Test
    public void term_simple_contains() {
        assertInput("pkg~=*ab*").asTerm().matchesFiles("a.b.ab#Bbb");
        assertInput("pkg~=*y.*").asTerm().matchesFiles("x.y.z#Util", "x.y.z#Util$1");
        assertInput("cls~=*in*").asTerm().matchesFiles("a.b.c#Main", "a.b.c#Main$1");
        assertInput("cls~=*$*").asTerm().matchesFiles("a.b.c#Main$1", "foo.bar.nest#SmallTest.Nested$2", "x.y.z#Util$1");
    }

    @Test
    public void term_joint_rules() {
        assertInput("pkg=x.y,pkg=x.y.z").asTerm().matchesFiles("x.y#Xy", "x.y.z#Util", "x.y.z#Util$1");
        assertInput("pkg=a,pkg=a.bc,pkg=b").asTerm().matchesFiles("a#Module", "a.bc#Ccc");
        assertInput("cls=Main,cls=Util").asTerm().matchesFiles("a.b.c#Main", "a.b.c#Util", "x.y.z#Util");
        assertInput("pkg~=a*,cls=Util").asTerm().matchesFiles("a.b.c#Util");
        assertInput("pkg~=a*,pkg~=x*,cls=Util").asTerm().matchesFiles("a.b.c#Util", "x.y.z#Util");
        assertInput("pkg~=a*,cls~=*$*").asTerm().matchesFiles("a.b.c#Main$1");
        assertInput("pkg~=*b*,cls~=*$*").asTerm().matchesFiles("a.b.c#Main$1", "foo.bar.nest#SmallTest.Nested$2");
    }

    @Test
    public void expr_simple_brackets() {
        assertInput("pkg=a.bc").asExpr().matchesFiles("a.bc#Ccc");
        assertInput("(pkg=a.bc)").asExpr().matchesFiles("a.bc#Ccc");
        assertInput("((pkg=a.bc))").asExpr().matchesFiles("a.bc#Ccc");
        assertInput("(((pkg=a.bc)))").asExpr().matchesFiles("a.bc#Ccc");
    }

    @Test
    public void expr_simple_boolean_and() {
        assertInput("(pkg=a.b.c)&(cls=Util)").asExpr().matchesFiles("a.b.c#Util");
        assertInput("((pkg=a.b.c)&(cls=Util))").asExpr().matchesFiles("a.b.c#Util");
        assertInput("(((pkg=a.b.c))&((cls=Util)))").asExpr().matchesFiles("a.b.c#Util");
    }

    @Test
    public void expr_simple_boolean_or() {
        assertInput("(pkg=a.b.c)|(cls=Main)").asExpr().matchesFiles("a.b.c#Main", "a.b.c#Main$1", "a.b.c#Util");
        assertInput("(pkg=abc)|(cls=Main)").asExpr().matchesFiles("a.b.c#Main");
        assertInput("(pkg=a.b.c)|(cls=No)").asExpr().matchesFiles("a.b.c#Main", "a.b.c#Main$1", "a.b.c#Util");
        assertInput("(pkg~=a.b.*)|(pkg=)").asExpr().isEquivalentTo((pkg, cls) -> pkg.startsWith("a.b.") || pkg.isEmpty());
    }

    @Test
    public void expr_simple_boolean_not() {
        assertInput("!(pkg~=*.*)").asExpr().isEquivalentTo((pkg, cls) -> !pkg.contains("."));
        assertInput("!(pkg~=*bar*)").asExpr().isEquivalentTo((pkg, cls) -> !pkg.contains("bar"));
        assertInput("!(cls=Main)").asExpr().isEquivalentTo((pkg, cls) -> !cls.equals("Main"));
        assertInput("!(cls~=*Test)").asExpr().isEquivalentTo((pkg, cls) -> !cls.endsWith("Test"));
    }

    @CheckReturnValue
    private static @NotNull InputSubject assertInput(@NotNull String input) {
        return new InputSubject(input);
    }

    @CheckReturnValue
    private static @NotNull FilterSubject assertFilter(@NotNull ClassFilter filter) {
        return new FilterSubject(filter);
    }

    @CanIgnoreReturnValue
    private record InputSubject(@NotNull String input) {
        public @NotNull FilterSubject asExpr() {
            ClassFilter filter = firstNonNull(new PropertyParser(input).parse(), ClassFilter.DEFAULT);
            return assertFilter(filter);
        }

        public @NotNull FilterSubject asTerm() {
            ClassFilter filter = new PropertyParser(input).parseTerm();
            return assertFilter(filter);
        }
    }

    @CanIgnoreReturnValue
    private record FilterSubject(@NotNull ClassFilter filter) {
        public @NotNull FilterSubject isEquivalentTo(@NotNull ClassFilter other) {
            assertThat(matchFiles(filter)).isEqualTo(matchFiles(other));
            return this;
        }

        public @NotNull FilterSubject isEquivalentTo(@NotNull ClassNamePredicate predicate) {
            return isEquivalentTo(ClassFilter.of(predicate));
        }

        public @NotNull FilterSubject matchesFiles(@NotNull String @NotNull ... files) {
           assertThat(matchFiles(filter)).containsExactlyElementsIn(files);
           return this;
        }

        public @NotNull FilterSubject matchesAll() {
            assertThat(matchFiles(filter)).containsAtLeastElementsIn(CLASSPATH);
            return this;
        }

        public @NotNull FilterSubject matchesNothing() {
            assertThat(matchFiles(filter)).isEmpty();
            return this;
        }

        private static @NotNull List<String> matchFiles(@NotNull ClassFilter filter) {
            return CLASSPATH.stream().filter(fullName -> {
                assert fullName.contains("#") : "Forgot the separator in the test data. Expected `#`, found: " + fullName;
                Pair<String, String> pair = Pair.of(fullName.split("#"));
                return filter.test(pair.first(), pair.second());
            }).toList();
        }
    }

    private static void assertFilterRoundtrip(@NotNull ClassFilter input) {
        String forward = ClassFilterStringConverter.TO_STRING.forward(input);
        ClassFilter backward = ClassFilterStringConverter.TO_STRING.backward(forward);
        assertFilter(backward).isEquivalentTo(input);
    }

    private static void assertBase64Roundtrip(@NotNull String expected, @NotNull String prefix, @NotNull Charset charset) {
        byte[] decoded = assertBase64Roundtrip(expected.getBytes(charset), prefix, charset);
        String actual = new String(decoded, charset);
        assertThat(actual).isEqualTo(expected);
    }

    private static byte[] assertBase64Roundtrip(byte @NotNull [] expected, @NotNull String prefix, @NotNull Charset charset) {
        String encoded = ClassFilterStringConverter.encodeBase64(expected, prefix, charset);
        byte[] decoded = ClassFilterStringConverter.decodeBase64(encoded, prefix, charset);
        assertThat(decoded).isEqualTo(expected);
        assertReversibleRoundtrip(ClassFilterStringConverter.BASE64_CONVERTER, expected);
        return decoded;
    }
}
