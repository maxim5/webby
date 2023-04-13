package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.webby.orm.arch.M2mInfo;
import io.webby.orm.arch.model.TableArch;
import io.webby.orm.arch.model.TableField;
import io.webby.orm.codegen.ModelInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public class ArchFactory {
    private final RunContext runContext;

    public ArchFactory(@NotNull RunContext runContext) {
        this.runContext = runContext;
    }

    public void build() {
        for (ModelInput input : runContext.inputs()) {
            TableArch table = buildShallowTable(input);
            input.keys().forEach(key -> runContext.tables().putTable(key, table));
        }
        for (ModelInput input : runContext.inputs()) {
            completeTable(input, runContext.tables().getTableOrDie(input.modelClass()));
        }
    }

    @VisibleForTesting
    @NotNull TableArch buildShallowTable(@NotNull ModelInput input) {
        return new TableArch(
            input.sqlName(), input.javaTableName(), input.modelClass(), input.javaModelName(),
            M2mInfo.fromModelClass(input.modelClass())
        );
    }

    @VisibleForTesting
    void completeTable(@NotNull ModelInput input, @NotNull TableArch table) {
        ImmutableList<TableField> fields = JavaClassAnalyzer.getAllFieldsOrdered(input.modelClass()).stream()
            .map(field -> new TableFieldArchFactory(runContext, table, field, input).buildTableField())
            .collect(ImmutableList.toImmutableList());
        table.initializeOrDie(fields);
        table.validate();
    }
}
