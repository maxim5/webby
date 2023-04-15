package io.webby.orm.api;

import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.webby.orm.api.TableMeta.ConstraintStatus.NO_CONSTRAINT;

/**
 * Holds the meta information of the SQL table.
 */
public interface TableMeta {
    /**
     * Returns the table name in SQL.
     */
    @NotNull String sqlTableName();

    /**
     * Returns the meta info for all columns in the table.
     */
    @NotNull List<ColumnMeta> sqlColumns();

    /**
     * Holds the meta information of the SQL column.
     */
    record ColumnMeta(@NotNull Column column,
                      @NotNull Class<?> type,
                      @NotNull ConstraintStatus primaryKey,
                      @NotNull ConstraintStatus unique,
                      boolean foreignKey,
                      boolean nullable,
                      @Nullable String defaultValue) {
        public static @NotNull ColumnMeta of(@NotNull Column column, @NotNull Class<?> type) {
            return new ColumnMeta(column, type, NO_CONSTRAINT, NO_CONSTRAINT, false, false, null);
        }

        public @NotNull String name() {
            return column.name();
        }

        public boolean isPrimaryKey() {
            return primaryKey != NO_CONSTRAINT;
        }

        public boolean isUnique() {
            return unique != NO_CONSTRAINT;
        }

        public boolean isForeignKey() {
            return foreignKey;
        }

        public boolean isNullable() {
            return nullable;
        }

        public boolean isNotNull() {
            return !nullable;
        }

        public boolean hasDefault() {
            return defaultValue != null;
        }

        public @NotNull ColumnMeta withPrimaryKey(@NotNull ConstraintStatus status) {
            return new ColumnMeta(column, type, status, unique, foreignKey, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withUnique(@NotNull ConstraintStatus status) {
            return new ColumnMeta(column, type, primaryKey, status, foreignKey, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withForeignKey(boolean foreignKey) {
            return new ColumnMeta(column, type, primaryKey, unique, foreignKey, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withNullable(boolean nullable) {
            return new ColumnMeta(column, type, primaryKey, unique, foreignKey, nullable, defaultValue);
        }

        public @NotNull ColumnMeta withDefault(@NotNull String defaultValue) {
            return new ColumnMeta(column, type, primaryKey, unique, foreignKey, nullable, defaultValue);
        }
    }

    enum ConstraintStatus {
        NO_CONSTRAINT, SINGLE_COLUMN, COMPOSITE;

        public boolean isSingle() {
            return this == SINGLE_COLUMN;
        }
    }

    /**
     * Returns the primary key constraint.
     */
    @NotNull Constraint primaryKeys();

    /**
     * Returns the collection of unique constraints.
     */
    @NotNull Iterable<Constraint> unique();

    /**
     * Holds the constraint that spans across one or more columns.
     */
    record Constraint(@NotNull List<Column> columns, boolean isComposite) {
        public static @NotNull Constraint of(@NotNull Column @NotNull ... columns) {
            return new Constraint(List.of(columns), columns.length > 1);
        }

        public boolean isSingle() {
            return !isComposite;
        }
    }
}
