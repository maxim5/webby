package io.spbx.orm.arch.factory;

import io.spbx.orm.api.Foreign;
import io.spbx.orm.api.ForeignInt;
import io.spbx.orm.api.ForeignLong;
import io.spbx.orm.api.ForeignObj;
import io.spbx.orm.arch.model.JdbcType;
import io.spbx.orm.arch.model.TableArch;
import io.spbx.orm.arch.util.AnnotationsAnalyzer;
import io.spbx.orm.arch.util.JavaClassAnalyzer;
import io.spbx.util.base.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.spbx.orm.arch.InvalidSqlModelException.assure;
import static io.spbx.orm.arch.InvalidSqlModelException.failIf;
import static io.spbx.util.reflect.EasyGenerics.getGenericTypeArgumentsOfField;
import static java.util.Objects.requireNonNull;

class ForeignTableArchResolver {
    private final RunContext runContext;

    public ForeignTableArchResolver(@NotNull RunContext runContext) {
        this.runContext = runContext;
    }

    public @Nullable Pair<TableArch, JdbcType> findForeignTableInfo(@NotNull Field field) {
        Class<?> fieldType = field.getType();
        if (!Foreign.class.isAssignableFrom(fieldType)) {
            return null;
        }

        assure(fieldType == Foreign.class || fieldType == ForeignInt.class ||
               fieldType == ForeignLong.class || fieldType == ForeignObj.class,
               "Invalid foreign key reference type: `%s`. Supported types: Foreign, ForeignInt, ForeignLong, ForeignObj",
               fieldType.getSimpleName());
        Type[] actualTypeArguments = getGenericTypeArgumentsOfField(field);
        Class<?> keyType =
            fieldType == ForeignInt.class ?
                int.class :
                fieldType == ForeignLong.class ?
                    long.class :
                    (Class<?>) actualTypeArguments[0];
        Class<?> entityType = (Class<?>) actualTypeArguments[actualTypeArguments.length - 1];

        TableArch foreignTable = runContext.tables().getTable(entityType);
        failIf(foreignTable == null,
               "Foreign model `%s` referenced from `%s` model is missing in the input set for table generation",
               entityType.getSimpleName(), field.getDeclaringClass().getSimpleName());

        Class<?> primaryKeyType;
        if (foreignTable.isInitialized()) {
            assure(foreignTable.hasPrimaryKeyField(),
                   "Foreign model `%s` does not have a primary key. Expected key type: `%s`",
                   entityType.getSimpleName(), keyType);
            primaryKeyType = requireNonNull(foreignTable.primaryKeyField()).javaType();
        } else {
            // A hacky way to get the PK field for a foreign model that hasn't been built yet.
            // Essentially, this duplicates the work of `buildTableField` above.
            // In theory, should work for dependency cycles.
            Class<?> foreignModelClass = field.getDeclaringClass();
            ModelInput foreignInput = runContext.inputs().findInputByModel(foreignModelClass).orElseThrow();
            Optional<Field> foreignPrimaryKeyField = JavaClassAnalyzer.getAllFieldsOrdered(foreignModelClass).stream()
                .filter(f -> AnnotationsAnalyzer.isPrimaryKeyField(f, foreignInput))
                .findFirst();
            assure(foreignPrimaryKeyField.isPresent(),
                   "Foreign model `%s` does not have a primary key. Expected key type: `%s`",
                   entityType.getSimpleName(), keyType);
            primaryKeyType = foreignPrimaryKeyField.get().getType();
        }

        assure(primaryKeyType == keyType,
               "Foreign model `%s` primary key `%s` doesn't match the foreign key",
               entityType.getSimpleName(), primaryKeyType, keyType);

        JdbcType jdbcType = JdbcType.findByMatchingNativeType(primaryKeyType);
        failIf(jdbcType == null,
               "Foreign model `%s` primary key `%s` must be natively supported type (i.e., primitive or String)",
               entityType.getSimpleName(), primaryKeyType, keyType);

        return Pair.of(foreignTable, jdbcType);
    }
}
