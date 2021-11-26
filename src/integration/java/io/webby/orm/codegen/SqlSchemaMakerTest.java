package io.webby.orm.codegen;

import io.webby.auth.session.SessionTable;
import io.webby.auth.user.UserTable;
import io.webby.db.model.BlobKvTable;
import io.webby.db.sql.testing.TableHelper;
import io.webby.orm.api.Engine;
import org.junit.jupiter.api.Test;

import static io.webby.orm.codegen.SqlSchemaMaker.makeCreateTableQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlSchemaMakerTest {
    @Test
    public void create_table_sqlite() {
        assertEquals(TableHelper.CREATE_USER_TABLE_SQL, makeCreateTableQuery(Engine.SQLite, UserTable.class));
        assertEquals(TableHelper.CREATE_SESSION_TABLE_SQL, makeCreateTableQuery(Engine.SQLite, SessionTable.class));
        assertEquals(TableHelper.CREATE_BLOB_KV_TABLE_SQL, makeCreateTableQuery(Engine.SQLite, BlobKvTable.class));
    }
}
