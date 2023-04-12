package io.webby.orm.arch;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

class PojoArchCollector {
    private final Map<Class<?>, PojoArch> pojos = new LinkedHashMap<>();

    public @NotNull Collection<AdapterArch> getAdapterArches() {
        return pojos.values().stream().map(AdapterArch::new).toList();
    }

    public @NotNull PojoArch getOrCompute(@NotNull Class<?> type, @NotNull Supplier<PojoArch> compute) {
        PojoArch pojo = pojos.get(type);
        if (pojo == null) {
            pojo = compute.get();
            pojos.put(type, pojo);
        }
        return pojo;
    }
}
