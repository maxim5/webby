package io.webby.orm.api.query;

import io.webby.orm.api.TableMeta;
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
                new ColumnMeta("id", int.class, F, false),
                new ColumnMeta("name", String.class, false, false),
                new ColumnMeta("country", String.class, false, false),
                new ColumnMeta("sex", boolean.class, false, false),
                new ColumnMeta("birthday", Timestamp.class, false, false),
                new ColumnMeta("iq", int.class, false, false),
                new ColumnMeta("height", double.class, false, false),
                new ColumnMeta("photo", byte[].class, false, false)
            );
        }
    };

    public static final boolean F = true;
    public static final boolean M = false;

    public static @NotNull Timestamp parseDate(@NotNull String date) {
        return Timestamp.valueOf(date + " 00:00:00");
    }

    public static byte @NotNull [] photo(int value) {
        return Integer.toHexString(value).getBytes();
    }
}
