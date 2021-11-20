package io.webby.app;

import com.google.common.base.Strings;
import io.webby.common.Packages;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiPredicate;

public final class ClassFilter {
    private static final BiPredicate<String, String> EXCLUDE_IMPLEMENTATION_PACKAGES =
            (pkg, __) -> !pkg.startsWith(Packages.RENDER_IMPL) &&
                         !pkg.startsWith(Packages.DB_KV_IMPL + ".") &&  // the dot means subpackages
                         !pkg.startsWith(Packages.DB_SQL_IMPL);

    private BiPredicate<String, String> predicate;

    ClassFilter() {
        this.predicate = null;
    }

    public ClassFilter(@NotNull BiPredicate<String, String> predicate) {
        this.predicate = predicate.and(EXCLUDE_IMPLEMENTATION_PACKAGES);
    }

    public @NotNull BiPredicate<String, String> predicateOrDefault() {
        return predicate != null ? predicate : EXCLUDE_IMPLEMENTATION_PACKAGES;
    }

    public boolean isSet() {
        return predicate != null;
    }

    public void setPredicate(@NotNull BiPredicate<String, String> predicate) {
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

    public void setCommonPackageOf(@NotNull List<Class<?>> classes) {
        setPackageOnly(getCommonPackage(classes));
    }

    public void setPredicateUnsafe(@NotNull BiPredicate<String, String> predicate) {
        this.predicate = predicate;
    }

    public static @NotNull ClassFilter matchingAnyOf(@NotNull ClassFilter first, @NotNull ClassFilter second) {
        return new ClassFilter(first.predicateOrDefault().or(second.predicateOrDefault()));
    }

    public static @NotNull ClassFilter matchingAllOf(@NotNull ClassFilter first, @NotNull ClassFilter second) {
        return new ClassFilter(first.predicateOrDefault().and(second.predicateOrDefault()));
    }

    private static @NotNull BiPredicate<String, String> onlyClass(@NotNull Class<?> klass) {
        return (pkg, cls) -> pkg.equals(klass.getPackageName()) && cls.equals(klass.getSimpleName());
    }

    private static @NotNull BiPredicate<String, String> allInPackage(@NotNull String packageName) {
        return (pkg, cls) -> pkg.startsWith(packageName);
    }

    private static @NotNull String getCommonPackage(@NotNull List<Class<?>> classes) {
        return classes.stream().map(Class::getPackageName).reduce(Strings::commonPrefix).orElse("");
    }
}
