package io.webby.app;

import com.google.common.base.Strings;
import io.webby.util.classpath.ClassNamePredicate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClassFilter implements ClassNamePredicate {
    private static final ClassNamePredicate EXCLUDE_IMPLEMENTATION_PACKAGES =
            (pkg, __) -> !pkg.startsWith(Packages.RENDER_IMPL) &&
                         !pkg.startsWith(Packages.DB_KV_IMPL + ".") &&  // the dot means subpackages
                         !pkg.startsWith(Packages.DB_SQL_IMPL);

    private ClassNamePredicate predicate;

    ClassFilter() {
        this.predicate = null;
    }

    ClassFilter(@NotNull ClassNamePredicate predicate) {
        this.predicate = predicate.and(EXCLUDE_IMPLEMENTATION_PACKAGES);
    }

    public static @NotNull ClassFilter empty() {
        return new ClassFilter();
    }

    public static @NotNull ClassFilter of(@NotNull ClassNamePredicate filter) {
        return new ClassFilter(filter);
    }

    @Override
    public boolean test(@NotNull String packageName, @NotNull String simpleClassName) {
        return predicateOrDefault().test(packageName, simpleClassName);
    }

    public @NotNull ClassNamePredicate predicateOrDefault() {
        return predicate != null ? predicate : EXCLUDE_IMPLEMENTATION_PACKAGES;
    }

    public boolean isSet() {
        return predicate != null;
    }

    public void setPredicate(@NotNull ClassNamePredicate predicate) {
        this.predicate = predicate.and(EXCLUDE_IMPLEMENTATION_PACKAGES);
    }

    public void setWholeClasspath() {
        this.predicate = EXCLUDE_IMPLEMENTATION_PACKAGES;
    }

    public void setPackageOnly(@NotNull String packageName) {
        setPredicate(allInPackage(packageName));
    }

    public void setSingleClassOnly(@NotNull Class<?> klass) {
        setPredicate(onlyClass(klass));
    }

    public void setPackagesOf(@NotNull List<Class<?>> classes) {
        setPredicate(packagesOf(classes));
    }

    public void setPackagesOf(@NotNull Class<?> @NotNull ... classes) {
        setPackagesOf(List.of(classes));
    }

    public void setCommonPackageOf(@NotNull List<Class<?>> classes) {
        setPackageOnly(getCommonPackage(classes));
    }

    public void setCommonPackageOf(@NotNull Class<?> @NotNull... classes) {
        setCommonPackageOf(List.of(classes));
    }

    public void setPredicateUnsafe(@NotNull ClassNamePredicate predicate) {
        this.predicate = predicate;
    }

    public static @NotNull ClassFilter matchingAnyOf(@NotNull ClassFilter first, @NotNull ClassFilter second) {
        return new ClassFilter(first.predicateOrDefault().or(second.predicateOrDefault()));
    }

    public static @NotNull ClassFilter matchingAllOf(@NotNull ClassFilter first, @NotNull ClassFilter second) {
        return new ClassFilter(first.predicateOrDefault().and(second.predicateOrDefault()));
    }

    private static @NotNull ClassNamePredicate onlyClass(@NotNull Class<?> klass) {
        return (pkg, cls) -> pkg.equals(klass.getPackageName()) && cls.equals(klass.getSimpleName());
    }

    private static @NotNull ClassNamePredicate packagesOf(@NotNull List<Class<?>> classes) {
        Set<String> packages = classes.stream().map(Class::getPackageName).collect(Collectors.toSet());
        return (pkg, cls) -> packages.contains(pkg);
    }

    private static @NotNull ClassNamePredicate allInPackage(@NotNull String packageName) {
        return (pkg, cls) -> pkg.startsWith(packageName);
    }

    private static @NotNull String getCommonPackage(@NotNull List<Class<?>> classes) {
        return classes.stream().map(Class::getPackageName).reduce(Strings::commonPrefix).orElse("");
    }
}
