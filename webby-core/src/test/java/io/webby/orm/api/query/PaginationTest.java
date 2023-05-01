package io.webby.orm.api.query;

import io.webby.orm.api.PageToken;
import io.webby.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.Pagination.*;
import static io.webby.testing.AssertBasics.assertEmpty;
import static io.webby.testing.AssertBasics.assertPresent;
import static org.junit.jupiter.api.Assertions.*;

public class PaginationTest {
    @Test
    public void init_valid_first_page() {
        Pagination pagination = firstPage(3);
        assertFalse(pagination.hasOffset());
        assertEquals(NO_OFFSET, pagination.offset());
        assertFalse(pagination.hasLastItem());
        assertNull(pagination.lastItem());
        assertEquals(3, pagination.limit());
    }

    @Test
    public void init_valid_offset() {
        Pagination pagination = ofOffset(10, 5);
        assertTrue(pagination.hasOffset());
        assertEquals(10, pagination.offset());
        assertFalse(pagination.hasLastItem());
        assertNull(pagination.lastItem());
        assertEquals(5, pagination.limit());
    }

    @Test
    public void init_valid_column_term() {
        Pagination pagination = ofColumn(FakeColumn.FOO.makeVar("x"), Order.ASC, 20);
        assertFalse(pagination.hasOffset());
        assertEquals(NO_OFFSET, pagination.offset());
        assertTrue(pagination.hasLastItem());
        assertEquals(FakeColumn.FOO.makeVar("x"), pagination.lastItem());
        assertEquals(20, pagination.limit());
        assertEquals(Order.ASC, pagination.order());
    }

    @Test
    public void init_valid_column_desc_term() {
        Pagination pagination = ofColumnDesc(FakeColumn.FOO.makeVar("x"), 30);
        assertFalse(pagination.hasOffset());
        assertEquals(NO_OFFSET, pagination.offset());
        assertTrue(pagination.hasLastItem());
        assertEquals(FakeColumn.FOO.makeVar("x"), pagination.lastItem());
        assertEquals(30, pagination.limit());
        assertEquals(Order.DESC, pagination.order());
    }

    @Test
    public void optional_chaining() {
        assertEmpty(PageToken.parseHumanToken("x").map(token -> ofOffsetIfMatches(token, 5)));
        assertEmpty(PageToken.parseHumanToken("1").map(token -> ofOffsetIfMatches(token, 5)));
        assertPresent(PageToken.parseHumanToken(":1").map(token -> ofOffsetIfMatches(token, 5)), ofOffset(1, 5));

        assertEmpty(PageToken.parseHumanToken(":1").map(token -> ofColumnIfMatches(token, FakeColumn.FOO, Order.ASC, 5)));
        assertEmpty(PageToken.parseHumanToken(":x").map(token -> ofColumnIfMatches(token, FakeColumn.FOO, Order.ASC, 5)));
        assertPresent(PageToken.parseHumanToken("x").map(token -> ofColumnIfMatches(token, FakeColumn.FOO, Order.ASC, 5)),
                      ofColumnAsc(FakeColumn.FOO.makeVar("x"), 5));
    }
}
