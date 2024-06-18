package io.spbx.orm.arch.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import javax.lang.model.SourceVersion;
import java.util.regex.Pattern;

public class JavaNameValidator {
    public static @NotNull String validateJavaIdentifier(@NotNull String name) {
        assert isValidJavaIdentifier(name) : "Invalid java identifier: " + name;
        return name;
    }

    public static @NotNull String validateJavaPackage(@NotNull String name) {
        assert isValidJavaPackage(name) : "Invalid java package: " + name;
        return name;
    }

    // https://stackoverflow.com/questions/29783092/regexp-to-match-java-package-name
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^(?:\\w+|\\w+\\.\\w+)+$");

    @VisibleForTesting
    static boolean isValidJavaIdentifier(@NotNull String name) {
        // https://stackoverflow.com/questions/15437866/how-to-validate-if-a-string-would-be-a-valid-java-variable
        return SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name);
    }

    @VisibleForTesting
    static boolean isValidJavaPackage(@NotNull String name) {
        return PACKAGE_PATTERN.matcher(name).matches();
    }
}
