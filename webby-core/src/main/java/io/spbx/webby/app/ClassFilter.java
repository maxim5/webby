package io.spbx.webby.app;

import com.google.common.base.Strings;
import io.spbx.util.classpath.ClassNamePredicate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Immutable
public final class ClassFilter implements ClassNamePredicate {
    private static final ClassNamePredicate EXCLUDE_IMPLEMENTATION_PACKAGES =
            (pkg, __) -> !pkg.startsWith(Packages.RENDER_IMPL) &&
                         !pkg.startsWith(Packages.DB_KV_IMPL + ".") &&  // the dot means subpackages
                         !pkg.startsWith(Packages.DB_SQL_IMPL);

    static final @NotNull ClassFilter ALL = of((packageName, simpleClassName) -> true);
    static final @NotNull ClassFilter NONE = of((packageName, simpleClassName) -> false);
    static final @NotNull ClassFilter DEFAULT = of(EXCLUDE_IMPLEMENTATION_PACKAGES);

    private final ClassNamePredicate predicate;

    private ClassFilter(@NotNull ClassNamePredicate predicate) {
        this.predicate = predicate;
    }

    public static @NotNull ClassFilter of(@NotNull ClassNamePredicate filter) {
        return new ClassFilter(filter);
    }

    public static @NotNull ClassFilter ofWholeClasspath() {
        return ALL;
    }

    public static @NotNull ClassFilter none() {
        return NONE;
    }

    public static @NotNull ClassFilter ofPackageTree(@NotNull String packageName) {
        return of((pkg, cls) -> pkg.startsWith(packageName));
    }

    public static @NotNull ClassFilter ofSingleClassOnly(@NotNull Class<?> klass) {
        return of((pkg, cls) -> pkg.equals(klass.getPackageName()) && cls.equals(klass.getSimpleName()));
    }

    public static @NotNull ClassFilter ofSelectedPackagesOnly(@NotNull Collection<String> packages) {
        return of((pkg, cls) -> packages.contains(pkg));
    }

    public static @NotNull ClassFilter ofSelectedPackagesOnly(@NotNull List<Class<?>> classes) {
        return ofSelectedPackagesOnly(classes.stream().map(Class::getPackageName).collect(Collectors.toSet()));
    }

    public static @NotNull ClassFilter ofSelectedPackagesOnly(@NotNull Class<?> @NotNull ... classes) {
        return ofSelectedPackagesOnly(List.of(classes));
    }

    public static @NotNull ClassFilter ofMostCommonPackageTree(@NotNull List<Class<?>> classes) {
        return ofPackageTree(classes.stream().map(Class::getPackageName).reduce(Strings::commonPrefix).orElse(""));
    }

    public static @NotNull ClassFilter ofMostCommonPackageTree(@NotNull Class<?> @NotNull... classes) {
        return ofMostCommonPackageTree(List.of(classes));
    }

    public static @NotNull ClassFilter matchingAnyOf(@NotNull ClassFilter first, @NotNull ClassFilter second) {
        return new ClassFilter(first.predicate.or(second.predicate));
    }

    public static @NotNull ClassFilter matchingAllOf(@NotNull ClassFilter first, @NotNull ClassFilter second) {
        return new ClassFilter(first.predicate.and(second.predicate));
    }

    public @NotNull ClassNamePredicate predicate() {
        return predicate;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    @Override
    public boolean test(@NotNull String packageName, @NotNull String simpleClassName) {
        return predicate.test(packageName, simpleClassName) &&
               EXCLUDE_IMPLEMENTATION_PACKAGES.test(packageName, simpleClassName);
    }
}
