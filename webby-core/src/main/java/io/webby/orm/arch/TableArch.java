package io.webby.orm.arch;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.webby.util.collect.Pair;
import io.webby.util.lazy.AtomicLazy;
import io.webby.util.lazy.AtomicLazyList;
import io.webby.orm.api.ReadFollow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.webby.orm.api.ReadFollow.FOLLOW_ALL;
import static io.webby.orm.api.ReadFollow.FOLLOW_ONE_LEVEL;
import static io.webby.orm.arch.InvalidSqlModelException.create;

@Immutable
public final class TableArch implements JavaNameHolder, WithColumns, WithPrefixedColumns {
    private final AtomicLazyList<TableField> fieldsRef = AtomicLazyList.ofUninitializedList();
    private final AtomicLazy<Optional<TableField>> primaryKeyCache = new AtomicLazy<>();

    private final String sqlName;
    private final String javaName;
    private final Class<?> modelClass;
    private final M2mInfo m2mInfo;

    public TableArch(@NotNull String sqlName,
                     @NotNull String javaName,
                     @NotNull Class<?> modelClass,
                     @Nullable M2mInfo m2mInfo) {
        this.sqlName = sqlName;
        this.javaName = javaName;
        this.modelClass = modelClass;
        this.m2mInfo = m2mInfo;
    }

    public @NotNull String sqlName() {
        return sqlName;
    }

    public @NotNull String javaName() {
        return javaName;
    }

    @Override
    public @NotNull String packageName() {
        return modelClass.getPackageName();
    }

    public @NotNull Class<?> modelClass() {
        return modelClass;
    }

    public @NotNull ImmutableList<TableField> fields() {
        return fieldsRef.getOrDie();
    }

    public boolean hasPrimaryKeyField() {
        return primaryKeyField() != null;
    }

    public @Nullable TableField primaryKeyField() {
        return primaryKeyCache.lazyGet(() -> fields().stream().filter(TableField::isPrimaryKey).findFirst()).orElse(null);
    }

    public boolean isPrimaryKeyInt() {
        TableField field = primaryKeyField();
        return field != null && field.javaType() == int.class;
    }

    public boolean isPrimaryKeyLong() {
        TableField field = primaryKeyField();
        return field != null && field.javaType() == long.class;
    }

    public boolean hasForeignKeyField() {
        return fields().stream().anyMatch(TableField::isForeignKey);
    }

    public @NotNull List<ForeignTableField> foreignFields(@NotNull ReadFollow follow) {
        return switch (follow) {
            case NO_FOLLOW -> List.of();
            case FOLLOW_ONE_LEVEL -> fields().stream()
                    .filter(TableField::isForeignKey)
                    .map(field -> (ForeignTableField) field).toList();
            case FOLLOW_ALL -> foreignFields(FOLLOW_ONE_LEVEL).stream()
                    .flatMap(field -> Stream.concat(Stream.of(field),
                                                    field.getForeignTable().foreignFields(FOLLOW_ALL).stream()))
                    .toList();
        };
    }

    @Override
    public @NotNull List<Column> columns() {
        return columns(tableField -> true);
    }

    public @NotNull List<Column> columns(@NotNull Predicate<TableField> fieldsFilter) {
        return fields().stream().filter(fieldsFilter).map(WithColumns::columns).flatMap(List::stream).toList();
    }

    @Override
    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow) {
        return columns(follow, tableField -> true);
    }

    public @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow, @NotNull Predicate<TableField> fieldsFilter) {
        return fields().stream().filter(fieldsFilter).flatMap(field -> field.columns(follow).stream()).toList();
    }

    public @NotNull List<Pair<TableField, Column>> columnsWithFields() {
        return fields().stream().flatMap(field -> field.columns().stream().map(column -> Pair.of(field, column))).toList();
    }

    public boolean isM2M() {
        return m2mInfo != null;
    }

    public @NotNull ForeignTableField m2mLeftFieldOrDie() {
        assert m2mInfo != null : "Internal error. Many-to-many not available for table: " + this;
        String leftName = m2mInfo.leftField();
        Stream<ForeignTableField> stream = foreignFields(FOLLOW_ONE_LEVEL).stream();
        if (leftName != null) {
            return stream.filter(field -> field.javaName().equals(leftName)).findFirst().orElseThrow(() ->
                create("Many-to-many left foreign-key field `%s` not found in model `%s`", leftName, describeModel())
            );
        } else {
            return stream.findFirst().orElseThrow(() ->
                create("Many-to-many right foreign-key field not found in model `%s`", describeModel())
            );
        }
    }

    public @NotNull ForeignTableField m2mRightFieldOrDie() {
        assert m2mInfo != null : "Internal error. Many-to-many not available for table: " + this;
        String rightName = m2mInfo.rightField();
        Stream<ForeignTableField> stream = foreignFields(FOLLOW_ONE_LEVEL).stream();
        if (rightName != null) {
            return stream.filter(field -> field.javaName().equals(rightName)).findFirst().orElseThrow(() ->
                create("Many-to-many right foreign-key field `%s` not found in model `%s`", rightName, describeModel())
            );
        } else {
            return stream.skip(1).findFirst().orElseThrow(() ->
                create("Many-to-many right foreign-key field not found in model `%s`", describeModel())
            );
        }
    }

    private @NotNull String describeModel() {
        return modelClass.getSimpleName();
    }

    /*package*/ boolean isInitialized() {
        return fieldsRef.isInitialized();
    }

    /*package*/ void initializeOrDie(@NotNull ImmutableList<TableField> fields) {
        fieldsRef.initializeOrDie(fields);
    }

    /*package*/ void validate() {
        if (isM2M()) {
            m2mLeftFieldOrDie();
            m2mRightFieldOrDie();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TableArch that &&
               Objects.equals(sqlName, that.sqlName) && Objects.equals(javaName, that.javaName) &&
               Objects.equals(modelClass, that.modelClass) && Objects.equals(m2mInfo, that.m2mInfo) &&
               Objects.equals(fieldsRef, that.fieldsRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlName, javaName, modelClass, m2mInfo, fieldsRef);
    }

    @Override
    public String toString() {
        return "TableArch[sqlName=%s, javaName=%s, modelClass=%s, m2mInfo=%s]"
                .formatted(sqlName, javaName, modelClass, m2mInfo);
    }
}
