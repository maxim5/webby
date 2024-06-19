package io.spbx.webby.testing;

import io.spbx.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public interface MaliciousTableTest<K, E, T extends TableObj<K, E>> extends PrimaryKeyTableTest<K, E, T> {
    @NotNull K[] maliciousKeys();
    
    @Test
    default void sql_injection_by_malicious_keys() {
        assumeKeys(1);

        K ok = keys()[0];
        E entityOk = createEntity(ok);
        table().insert(entityOk);

        for (K input : maliciousKeys()) {
            assertThat(table().getByPkOrNull(input)).isNull();
            assertThat(table().getByPkOrNull(ok)).isNotNull();
            assertThat(table().count()).isEqualTo(1);
            assertThat(table().fetchAll()).containsExactly(entityOk);

            E badEntity = createEntity(input, 0);
            assertThat(table().insert(badEntity)).isEqualTo(1);
            assertThat(table().getByPkOrNull(input)).isNotNull();
            assertThat(table().getByPkOrNull(ok)).isNotNull();
            assertThat(table().count()).isEqualTo(2);
            assertThat(table().fetchAll()).containsExactly(entityOk, badEntity);

            badEntity = createEntity(input, 1);
            assertThat(table().updateByPk(badEntity)).isEqualTo(1);
            assertThat(table().getByPkOrNull(input)).isNotNull();
            assertThat(table().getByPkOrNull(ok)).isNotNull();
            assertThat(table().count()).isEqualTo(2);
            assertThat(table().fetchAll()).containsExactly(entityOk, badEntity);

            assertThat(table().deleteByPk(input)).isEqualTo(1);
            assertThat(table().getByPkOrNull(input)).isNull();
            assertThat(table().getByPkOrNull(ok)).isNotNull();
            assertThat(table().count()).isEqualTo(1);
            assertThat(table().fetchAll()).containsExactly(entityOk);
        }
    }
}
