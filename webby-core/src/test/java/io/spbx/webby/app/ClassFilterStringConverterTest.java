package io.spbx.webby.app;

import io.spbx.util.base.CharArray;
import io.spbx.util.base.EasyPrimitives;
import io.spbx.util.base.Pair;
import io.spbx.webby.app.ClassFilterStringConverter.PropertyParser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.AssertBasics.assertReversibleRoundtrip;

public class ClassFilterStringConverterTest {
    private static final List<String> TREE = List.of(
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
    public void filter_roundtrip() {
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

    private static void assertFilterRoundtrip(@NotNull ClassFilter input) {
        String forward = ClassFilterStringConverter.INSTANCE.forward(input);
        ClassFilter backward = ClassFilterStringConverter.INSTANCE.backward(forward);
        assertThat(matchTree(backward)).isEqualTo(matchTree(input));
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

    @Test
    public void parseTerm_defaults() {
        assertThat(matchTree(parseTerm("all"))).containsExactlyElementsIn(TREE);
        assertThat(matchTree(parseTerm("none"))).isEmpty();
        assertThat(matchTree(parseTerm("default"))).containsExactlyElementsIn(TREE);
    }

    @Test
    public void parseTerm_simple_equals() {
        assertThat(matchTree(parseTerm("pkg="))).containsExactly("#TopLevel");
        assertThat(matchTree(parseTerm("pkg=x.y"))).containsExactly("x.y#Xy");
        assertThat(matchTree(parseTerm("cls=Xy"))).containsExactly("x.y#Xy");
        assertThat(matchTree(parseTerm("cls=Util"))).containsExactly("a.b.c#Util", "x.y.z#Util");
    }

    @Test
    public void parseTerm_simple_starts_with() {
        assertThat(matchTree(parseTerm("pkg~=*"))).containsExactlyElementsIn(TREE);
        assertThat(matchTree(parseTerm("pkg~=x.y*"))).containsExactly("x.y#Xy", "x.y.z#Util", "x.y.z#Util$1");
        assertThat(matchTree(parseTerm("cls~=Util*"))).containsExactly("a.b.c#Util", "x.y.z#Util", "x.y.z#Util$1");
        assertThat(matchTree(parseTerm("cls~=Main*"))).containsExactly("a.b.c#Main", "a.b.c#Main$1");
    }

    @Test
    public void parseTerm_simple_ends_with() {
        assertThat(matchTree(parseTerm("pkg~=*y"))).containsExactly("x.y#Xy");
        assertThat(matchTree(parseTerm("pkg~=*z"))).containsExactly("x.y.z#Util", "x.y.z#Util$1");
        assertThat(matchTree(parseTerm("cls~=*$1"))).containsExactly("a.b.c#Main$1", "x.y.z#Util$1");
        assertThat(matchTree(parseTerm("cls~=*l"))).containsExactly("a.b.c#Util", "x.y.z#Util", "#TopLevel");
        assertThat(matchTree(parseTerm("cls~=*$3"))).isEmpty();
    }

    @Test
    public void parseTerm_simple_contains() {
        assertThat(matchTree(parseTerm("pkg~=*ab*"))).containsExactly("a.b.ab#Bbb");
        assertThat(matchTree(parseTerm("pkg~=*y.*"))).containsExactly("x.y.z#Util", "x.y.z#Util$1");
        assertThat(matchTree(parseTerm("cls~=*in*"))).containsExactly("a.b.c#Main", "a.b.c#Main$1");
        assertThat(matchTree(parseTerm("cls~=*$*")))
            .containsExactly("a.b.c#Main$1", "foo.bar.nest#SmallTest.Nested$2", "x.y.z#Util$1");
    }

    @Test
    public void parseTerm_joint_rules() {
        assertThat(matchTree(parseTerm("pkg=x.y,pkg=x.y.z"))).containsExactly("x.y#Xy", "x.y.z#Util", "x.y.z#Util$1");
        assertThat(matchTree(parseTerm("pkg=a,pkg=a.bc,pkg=b"))).containsExactly("a#Module", "a.bc#Ccc");
        assertThat(matchTree(parseTerm("cls=Main,cls=Util"))).containsExactly("a.b.c#Main", "a.b.c#Util", "x.y.z#Util");
        assertThat(matchTree(parseTerm("pkg~=a*,cls=Util"))).containsExactly("a.b.c#Util");
        assertThat(matchTree(parseTerm("pkg~=a*,pkg~=x*,cls=Util"))).containsExactly("a.b.c#Util", "x.y.z#Util");
        assertThat(matchTree(parseTerm("pkg~=a*,cls~=*$*"))).containsExactly("a.b.c#Main$1");
        assertThat(matchTree(parseTerm("pkg~=*b*,cls~=*$*")))
            .containsExactly("a.b.c#Main$1", "foo.bar.nest#SmallTest.Nested$2");
    }

    private static @NotNull ClassFilter parseTerm(@NotNull String str) {
        return PropertyParser.parseTerm(new CharArray(str));
    }

    private static @NotNull List<String> matchTree(@NotNull ClassFilter filter) {
        return TREE.stream().filter(fullName -> {
            assert fullName.contains("#") : "Forgot the separator in the test data. Expected `#`, found: " + fullName;
            Pair<String, String> pair = Pair.of(fullName.split("#"));
            return filter.test(pair.first(), pair.second());
        }).toList();
    }
}
