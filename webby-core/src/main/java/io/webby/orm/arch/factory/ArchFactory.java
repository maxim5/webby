package io.webby.orm.arch.factory;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.arch.model.BridgeInfo;
import io.webby.orm.arch.model.PojoArch;
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
            for (ModelInput modelInput : runContext.inputs().models()) {
                TableArch table = buildShallowTable(modelInput);
                modelInput.keys().forEach(key -> runContext.tables().putTable(key, table));
            }
            for (ModelInput modelInput : runContext.inputs().models()) {
                completeTable(modelInput, runContext);
            }
            for (PojoInput pojoInput : runContext.inputs().pojos()) {
                buildPojo(pojoInput, runContext);
            }
            return new RunResult(runContext.tables().getAllTables(), runContext.pojos().getAdapterArches());
        } catch (RuntimeException e) {
            throw runContext.errorHandler().handleRuntimeException(e);
        }
    }

    private @NotNull TableArch buildShallowTable(@NotNull ModelInput modelInput) {
        return new TableArch(
            modelInput.sqlName(), modelInput.javaTableName(), modelInput.modelClass(), modelInput.javaModelName(),
            BridgeInfo.fromModelClass(modelInput.modelClass())
        );
    }

    private void completeTable(@NotNull ModelInput modelInput, @NotNull RunContext runContext) {
        runContext.errorHandler().setCurrentModel(modelInput);
        TableArch table = runContext.tables().getTableOrDie(modelInput.modelClass());

        ImmutableList<TableField> fields = JavaClassAnalyzer.getAllFieldsOrdered(modelInput.modelClass()).stream()
            .map(field -> {
                runContext.errorHandler().setCurrentField(field);
                return new TableFieldArchFactory(runContext, table, field, modelInput).buildTableField();
            })
            .collect(ImmutableList.toImmutableList());
        runContext.errorHandler().dropCurrentField();

        table.initializeOrDie(fields);
        table.validate();
    }

    @CanIgnoreReturnValue
    private @NotNull PojoArch buildPojo(@NotNull PojoInput pojoInput, @NotNull RunContext runContext) {
        return new RecursivePojoArchFactory(runContext).buildPojoArchFor(pojoInput.pojoClass());
    }
}
