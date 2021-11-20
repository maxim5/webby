package io.webby.testing;

import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public interface MaliciousTableTest<K, E, T extends TableObj<K, E>> extends PrimaryKeyTableTest<K, E, T> {
    @NotNull K[] maliciousKeys();
    
    @Test
    default void sql_injection_by_malicious_keys() {
        assumeKeys(1);

        K ok = keys()[0];
        E entityOk = createEntity(ok);
        table().insert(entityOk);

        for (K input : maliciousKeys()) {
            assertNull(table().getByPkOrNull(input));
            assertNotNull(table().getByPkOrNull(ok));
            assertEquals(1, table().count());
            assertThat(table().fetchAll()).containsExactly(entityOk);

            E badEntity = createEntity(input, 0);
            assertEquals(1, table().insert(badEntity));
            assertNotNull(table().getByPkOrNull(input));
            assertNotNull(table().getByPkOrNull(ok));
            assertEquals(2, table().count());
            assertThat(table().fetchAll()).containsExactly(entityOk, badEntity);

            badEntity = createEntity(input, 1);
            assertEquals(1, table().updateByPk(badEntity));
            assertNotNull(table().getByPkOrNull(input));
            assertNotNull(table().getByPkOrNull(ok));
            assertEquals(2, table().count());
            assertThat(table().fetchAll()).containsExactly(entityOk, badEntity);

            assertEquals(1, table().deleteByPk(input));
            assertNull(table().getByPkOrNull(input));
            assertNotNull(table().getByPkOrNull(ok));
            assertEquals(1, table().count());
            assertThat(table().fetchAll()).containsExactly(entityOk);
        }
    }
}
