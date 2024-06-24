package io.spbx.orm.arch.model;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.arch.model.JavaNameValidator.isValidJavaIdentifier;
import static io.spbx.orm.arch.model.JavaNameValidator.isValidJavaIdentifiersSeparatedByDots;

public class JavaNameValidatorTest {
    @Test
    public void variableNamesCanBeginWithLetters() {
        assertThat(isValidJavaIdentifier("test")).isTrue();
        assertThat(isValidJavaIdentifier("e2")).isTrue();
        assertThat(isValidJavaIdentifier("w")).isTrue();
        assertThat(isValidJavaIdentifier("привет")).isTrue();
    }

    @Test
    public void variableNamesCanBeginWithDollarSign() {
        assertThat(isValidJavaIdentifier("$test")).isTrue();
        assertThat(isValidJavaIdentifier("$e2")).isTrue();
        assertThat(isValidJavaIdentifier("$w")).isTrue();
        assertThat(isValidJavaIdentifier("$привет")).isTrue();
        assertThat(isValidJavaIdentifier("$")).isTrue();
        assertThat(isValidJavaIdentifier("$55")).isTrue();
    }

    @Test
    public void variableNamesCanBeginWithUnderscore() {
        assertThat(isValidJavaIdentifier("_test")).isTrue();
        assertThat(isValidJavaIdentifier("_e2")).isTrue();
        assertThat(isValidJavaIdentifier("_w")).isTrue();
        assertThat(isValidJavaIdentifier("_привет")).isTrue();
        assertThat(isValidJavaIdentifier("_55")).isTrue();
    }

    @Test
    public void variableNamesCannotContainCharactersThatAreNotLettersOrDigits() {
        assertThat(isValidJavaIdentifier("apple.color")).isFalse();
        assertThat(isValidJavaIdentifier("my var")).isFalse();
        assertThat(isValidJavaIdentifier(" ")).isFalse();
        assertThat(isValidJavaIdentifier("apple%color")).isFalse();
        assertThat(isValidJavaIdentifier("apple,color")).isFalse();
        assertThat(isValidJavaIdentifier(",applecolor")).isFalse();
    }

    @Test
    public void variableNamesCannotStartWithDigit() {
        assertThat(isValidJavaIdentifier("2e")).isFalse();
        assertThat(isValidJavaIdentifier("5")).isFalse();
        assertThat(isValidJavaIdentifier("123test")).isFalse();
    }


    @Test
    public void differentSourceVersionsAreHandledCorrectly() {
        assertThat(isValidJavaIdentifier("_")).isFalse();
        assertThat(isValidJavaIdentifier("enum")).isFalse();
    }

    @Test
    public void keywordsCannotBeUsedAsVariableNames() {
        assertThat(isValidJavaIdentifier("strictfp")).isFalse();
        assertThat(isValidJavaIdentifier("assert")).isFalse();
        assertThat(isValidJavaIdentifier("enum")).isFalse();

        // Modifiers
        assertThat(isValidJavaIdentifier("public")).isFalse();
        assertThat(isValidJavaIdentifier("protected")).isFalse();
        assertThat(isValidJavaIdentifier("private")).isFalse();

        assertThat(isValidJavaIdentifier("abstract")).isFalse();
        assertThat(isValidJavaIdentifier("static")).isFalse();
        assertThat(isValidJavaIdentifier("final")).isFalse();

        assertThat(isValidJavaIdentifier("transient")).isFalse();
        assertThat(isValidJavaIdentifier("volatile")).isFalse();
        assertThat(isValidJavaIdentifier("synchronized")).isFalse();

        assertThat(isValidJavaIdentifier("native")).isFalse();

        // Declarations
        assertThat(isValidJavaIdentifier("class")).isFalse();
        assertThat(isValidJavaIdentifier("interface")).isFalse();
        assertThat(isValidJavaIdentifier("extends")).isFalse();
        assertThat(isValidJavaIdentifier("package")).isFalse();
        assertThat(isValidJavaIdentifier("throws")).isFalse();
        assertThat(isValidJavaIdentifier("implements")).isFalse();

        // Primitive types and void
        assertThat(isValidJavaIdentifier("boolean")).isFalse();
        assertThat(isValidJavaIdentifier("byte")).isFalse();
        assertThat(isValidJavaIdentifier("char")).isFalse();
        assertThat(isValidJavaIdentifier("short")).isFalse();
        assertThat(isValidJavaIdentifier("int")).isFalse();
        assertThat(isValidJavaIdentifier("long")).isFalse();
        assertThat(isValidJavaIdentifier("float")).isFalse();
        assertThat(isValidJavaIdentifier("double")).isFalse();
        assertThat(isValidJavaIdentifier("void")).isFalse();

        // Control flow
        assertThat(isValidJavaIdentifier("if")).isFalse();
        assertThat(isValidJavaIdentifier("else")).isFalse();

        assertThat(isValidJavaIdentifier("try")).isFalse();
        assertThat(isValidJavaIdentifier("catch")).isFalse();
        assertThat(isValidJavaIdentifier("finally")).isFalse();

        assertThat(isValidJavaIdentifier("do")).isFalse();
        assertThat(isValidJavaIdentifier("while")).isFalse();
        assertThat(isValidJavaIdentifier("for")).isFalse();
        assertThat(isValidJavaIdentifier("continue")).isFalse();

        assertThat(isValidJavaIdentifier("switch")).isFalse();
        assertThat(isValidJavaIdentifier("case")).isFalse();
        assertThat(isValidJavaIdentifier("default")).isFalse();
        assertThat(isValidJavaIdentifier("break")).isFalse();
        assertThat(isValidJavaIdentifier("throw")).isFalse();

        assertThat(isValidJavaIdentifier("return")).isFalse();

        // Other keywords
        assertThat(isValidJavaIdentifier("this")).isFalse();
        assertThat(isValidJavaIdentifier("new")).isFalse();
        assertThat(isValidJavaIdentifier("super")).isFalse();
        assertThat(isValidJavaIdentifier("import")).isFalse();
        assertThat(isValidJavaIdentifier("instanceof")).isFalse();

        // Reserved keywords
        assertThat(isValidJavaIdentifier("goto")).isFalse();
        assertThat(isValidJavaIdentifier("const")).isFalse();
    }

    @Test
    public void literalsCannotBeUsedAsVariableNames() {
        assertThat(isValidJavaIdentifier("null")).isFalse();
        assertThat(isValidJavaIdentifier("true")).isFalse();
        assertThat(isValidJavaIdentifier("false")).isFalse();
    }

    @Test
    public void isValidJavaIdentifiersSeparatedByDots_simple() {
        assertThat(isValidJavaIdentifiersSeparatedByDots("com")).isTrue();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com.google")).isTrue();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com.google.test")).isTrue();

        assertThat(isValidJavaIdentifiersSeparatedByDots("")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots(".")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots("..")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots(". .")).isFalse();

        assertThat(isValidJavaIdentifiersSeparatedByDots(".com")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com.")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com..google")).isFalse();

        assertThat(isValidJavaIdentifiersSeparatedByDots("123")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots("int")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com.char.google")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com.google.enum")).isFalse();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com.google.0test")).isFalse();

        assertThat(isValidJavaIdentifiersSeparatedByDots("main")).isTrue();
        assertThat(isValidJavaIdentifiersSeparatedByDots("Main")).isTrue();
        assertThat(isValidJavaIdentifiersSeparatedByDots("MAIN")).isTrue();
        assertThat(isValidJavaIdentifiersSeparatedByDots("Main123")).isTrue();
        assertThat(isValidJavaIdentifiersSeparatedByDots("Main.Nested")).isTrue();
        assertThat(isValidJavaIdentifiersSeparatedByDots("com.google.Main.Nested")).isTrue();
    }
}
