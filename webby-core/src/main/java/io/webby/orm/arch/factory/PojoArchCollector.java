package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.webby.orm.arch.model.AdapterArch;
import io.webby.orm.arch.model.PojoArch;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

class PojoArchCollector {
    private final Map<Class<?>, PojoArch> pojos = new LinkedHashMap<>();

    public @NotNull ImmutableList<AdapterArch> getAdapterArches() {
        return pojos.values().stream().map(AdapterArch::new).collect(ImmutableList.toImmutableList());
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
