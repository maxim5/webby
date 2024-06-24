package io.spbx.orm.arch.model;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import javax.lang.model.SourceVersion;

public class JavaNameValidator {
    public static @NotNull String validateJavaIdentifier(@NotNull String name) {
        assert isValidJavaIdentifier(name) : "Invalid java identifier: " + name;
        return name;
    }

    public static @NotNull String validateJavaClassName(@NotNull String name) {
        assert isValidJavaIdentifiersSeparatedByDots(name) : "Invalid java class name: " + name;
        return name;
    }

    public static @NotNull String validateJavaPackage(@NotNull String name) {
        assert isValidJavaIdentifiersSeparatedByDots(name) : "Invalid java package: " + name;
        return name;
    }

    @VisibleForTesting
    static boolean isValidJavaIdentifier(@NotNull String name) {
        // https://stackoverflow.com/questions/15437866/how-to-validate-if-a-string-would-be-a-valid-java-variable
        return SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name);
    }

    @VisibleForTesting
    static boolean isValidJavaIdentifiersSeparatedByDots(@NotNull String name) {
        // See also https://stackoverflow.com/questions/29783092/regexp-to-match-java-package-name
        // Pattern.compile("^(?:\\w+|\\w+\\.\\w+)+$")
        return Splitter.on('.').splitToStream(name).allMatch(JavaNameValidator::isValidJavaIdentifier);
    }
}
