package io.webby.orm.api;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryExceptionTest {
    @Test
    public void message_format() {
        QueryException e = new QueryException("Original text", "SELECT count(*) FROM table\nWHERE val > 0", 1, 2);
        assertEquals(e.getMessage(), """
            Original text. Query:
            ```
            SELECT count(*) FROM table
            WHERE val > 0
            ```
            Args: `[1, 2]`""");
        assertEquals(e.getQuery(), "SELECT count(*) FROM table\nWHERE val > 0");
        assertThat(e.getArgs()).containsExactly(1, 2);
    }

    @Test
    public void args_list() {
        assertThat(new QueryException("Msg", "DROP x", List.of(1, 2), null).getArgs()).containsExactly(1, 2);
        assertThat(new QueryException("Msg", "DROP x", Set.of(1, 2), null).getArgs()).containsExactly(1, 2);
        assertThat(new QueryException("Msg", "DROP x", IntArrayList.from(1, 2), null).getArgs()).containsExactly(1, 2);
        assertThat(new QueryException("Msg", "DROP x", LongArrayList.from(1, 2), null).getArgs()).containsExactly(1L, 2L);
    }
}
