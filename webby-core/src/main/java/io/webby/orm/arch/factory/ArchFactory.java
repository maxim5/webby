package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import io.webby.orm.arch.model.BridgeInfo;
import io.webby.orm.arch.model.TableArch;
import io.webby.orm.arch.model.TableField;
import io.webby.orm.arch.util.JavaClassAnalyzer;
import io.webby.orm.codegen.ModelAdaptersScanner;
import org.jetbrains.annotations.NotNull;

public class ArchFactory {
    private final ModelAdaptersScanner locator;

    public ArchFactory(@NotNull ModelAdaptersScanner locator) {
        this.locator = locator;
    }

    public @NotNull RunResult build(@NotNull RunInputs inputs) {
        final RunContext runContext = new RunContext(inputs, locator);
        try (runContext) {
            for (ModelInput input : runContext.inputs()) {
                TableArch table = buildShallowTable(input);
                input.keys().forEach(key -> runContext.tables().putTable(key, table));
            }
            for (ModelInput input : runContext.inputs()) {
                completeTable(input, runContext);
            }
            return new RunResult(runContext.tables().getAllTables(), runContext.pojos().getAdapterArches());
        } catch (RuntimeException e) {
            throw runContext.errorHandler().handleRuntimeException(e);
        }
    }

    private @NotNull TableArch buildShallowTable(@NotNull ModelInput input) {
        return new TableArch(
            input.sqlName(), input.javaTableName(), input.modelClass(), input.javaModelName(),
            BridgeInfo.fromModelClass(input.modelClass())
        );
    }

    private void completeTable(@NotNull ModelInput input, @NotNull RunContext runContext) {
        runContext.errorHandler().setCurrentModel(input);
        TableArch table = runContext.tables().getTableOrDie(input.modelClass());

        ImmutableList<TableField> fields = JavaClassAnalyzer.getAllFieldsOrdered(input.modelClass()).stream()
            .map(field -> {
                runContext.errorHandler().setCurrentField(field);
                return new TableFieldArchFactory(runContext, table, field, input).buildTableField();
            })
            .collect(ImmutableList.toImmutableList());
        runContext.errorHandler().dropCurrentField();

        table.initializeOrDie(fields);
        table.validate();
    }
}
