package io.webby.orm.testing;

import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.Column;
import io.webby.orm.api.query.TermType;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.List;

public class PersonTableData {
    public enum PersonColumn implements Column {
        id(TermType.NUMBER),
        name(TermType.STRING),
        country(TermType.STRING),
        sex(TermType.BOOL),
        birthday(TermType.TIME),
        iq(TermType.NUMBER),
        height(TermType.NUMBER),
        photo(TermType.STRING);

        private final TermType type;

        PersonColumn(TermType type) {
            this.type = type;
        }

        @Override
        public @NotNull TermType type() {
            return type;
        }
    }

    public static final TableMeta PERSON_META = new TableMeta() {
        @Override
        public @NotNull String sqlTableName() {
            return "person";
        }

        @Override
        public @NotNull List<ColumnMeta> sqlColumns() {
            return List.of(
                new ColumnMeta(PersonColumn.id, int.class, true, false),
                new ColumnMeta(PersonColumn.name, String.class, false, false),
                new ColumnMeta(PersonColumn.country, String.class, false, false),
                new ColumnMeta(PersonColumn.sex, boolean.class, false, false),
                new ColumnMeta(PersonColumn.birthday, Timestamp.class, false, false),
                new ColumnMeta(PersonColumn.iq, int.class, false, false),
                new ColumnMeta(PersonColumn.height, double.class, false, false),
                new ColumnMeta(PersonColumn.photo, byte[].class, false, false)
            );
        }

        @Override
        public @NotNull Constraint primaryKeys() {
            return Constraint.of(PersonColumn.id);
        }

        @Override
        public @NotNull Iterable<Constraint> unique() {
            return List.of();
        }
    };

    public static final boolean FEMALE = true;
    public static final boolean MALE = false;

    public static @NotNull Timestamp parseDate(@NotNull String date) {
        return Timestamp.valueOf(date + " 00:00:00");
    }

    public static byte @NotNull [] photo(int value) {
        return Integer.toHexString(value).getBytes();
    }
}
