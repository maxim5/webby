package io.webby.orm.api.query;

import io.webby.auth.session.SessionTable;
import io.webby.auth.user.UserTable;
import io.webby.db.model.BlobKvTable;
import io.webby.demo.model.*;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class CreateTableQueryTest {
    @Test
    public void create_table_user() {
        assertThat(createTableIfNotExists(Engine.SQLite, UserTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER NOT NULL PRIMARY KEY,
                created_at INTEGER NOT NULL,
                access_level INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, UserTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                created_at TIMESTAMP NOT NULL,
                access_level INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, UserTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                created_at TIMESTAMP(3) NOT NULL,
                access_level INTEGER NOT NULL
            )
            """);
    }

    @Test
    public void create_table_session() {
        assertThat(createTableIfNotExists(Engine.SQLite, SessionTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id INTEGER NOT NULL PRIMARY KEY,
                user_id INTEGER,
                created_at INTEGER NOT NULL,
                user_agent VARCHAR NOT NULL,
                ip_address VARCHAR,
                FOREIGN KEY(user_id) REFERENCES user(user_id)
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, SessionTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                user_id INTEGER,
                created_at TIMESTAMP NOT NULL,
                user_agent VARCHAR NOT NULL,
                ip_address VARCHAR,
                FOREIGN KEY(user_id) REFERENCES user(user_id)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, SessionTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                user_id INTEGER,
                created_at TIMESTAMP(3) NOT NULL,
                user_agent VARCHAR(4096) NOT NULL,
                ip_address VARCHAR(4096),
                FOREIGN KEY(user_id) REFERENCES user(user_id)
            )
            """);
    }

    @Test
    public void create_table_blob_kv() {
        assertThat(createTableIfNotExists(Engine.SQLite, BlobKvTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR NOT NULL PRIMARY KEY,
                value BLOB
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, BlobKvTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR NOT NULL PRIMARY KEY,
                value BLOB
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, BlobKvTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARBINARY(255) NOT NULL PRIMARY KEY,
                value BLOB
            )
            """);
    }

    // Example models

    @Test
    public void create_table_atomic_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, AtomicModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER NOT NULL PRIMARY KEY,
                i INTEGER NOT NULL,
                l INTEGER NOT NULL,
                b INTEGER NOT NULL,
                s VARCHAR
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, AtomicModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b BOOLEAN NOT NULL,
                s VARCHAR
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, AtomicModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b BOOLEAN NOT NULL,
                s VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_complex_id_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, ComplexIdModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS complex_id_model (
                id_x INTEGER NOT NULL,
                id_y INTEGER NOT NULL,
                id_z VARCHAR NOT NULL,
                a INTEGER NOT NULL,
                PRIMARY KEY (id_x, id_y, id_z)
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, ComplexIdModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS complex_id_model (
                id_x INTEGER NOT NULL,
                id_y BIGINT NOT NULL,
                id_z VARCHAR NOT NULL,
                a INTEGER NOT NULL,
                PRIMARY KEY (id_x, id_y, id_z)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, ComplexIdModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS complex_id_model (
                id_x INTEGER NOT NULL,
                id_y BIGINT NOT NULL,
                id_z VARCHAR(255) NOT NULL,
                a INTEGER NOT NULL,
                PRIMARY KEY (id_x, id_y, id_z)
            )
            """);
    }

    @Test
    public void create_table_constraints_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, ConstraintsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS constraints_model (
                key_id INTEGER NOT NULL PRIMARY KEY,
                fprint INTEGER NOT NULL DEFAULT (0) UNIQUE,
                range_from INTEGER NOT NULL,
                range_to INTEGER NOT NULL,
                name VARCHAR NOT NULL,
                s VARCHAR,
                UNIQUE (range_from, range_to)
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, ConstraintsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS constraints_model (
                key_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                fprint INTEGER NOT NULL DEFAULT (0) UNIQUE,
                range_from INTEGER NOT NULL,
                range_to INTEGER NOT NULL,
                name VARCHAR NOT NULL,
                s VARCHAR,
                UNIQUE (range_from, range_to)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, ConstraintsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS constraints_model (
                key_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                fprint INTEGER NOT NULL DEFAULT (0) UNIQUE,
                range_from INTEGER NOT NULL,
                range_to INTEGER NOT NULL,
                name VARCHAR(4096) NOT NULL,
                s VARCHAR(4096),
                UNIQUE (range_from, range_to)
            )
            """);
    }

    @Test
    public void create_table_deep_nested_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, DeepNestedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS deep_nested_model (
                id INTEGER NOT NULL PRIMARY KEY,
                d_id INTEGER NOT NULL,
                d_c_id INTEGER NOT NULL,
                d_c_a_id INTEGER NOT NULL,
                d_c_b_id INTEGER NOT NULL,
                d_c_b_a_id INTEGER NOT NULL,
                d_c_aa_id INTEGER NOT NULL,
                d_b_id INTEGER NOT NULL,
                d_b_a_id INTEGER NOT NULL,
                a_id INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, DeepNestedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS deep_nested_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                d_id INTEGER NOT NULL,
                d_c_id INTEGER NOT NULL,
                d_c_a_id INTEGER NOT NULL,
                d_c_b_id INTEGER NOT NULL,
                d_c_b_a_id INTEGER NOT NULL,
                d_c_aa_id INTEGER NOT NULL,
                d_b_id INTEGER NOT NULL,
                d_b_a_id INTEGER NOT NULL,
                a_id INTEGER NOT NULL
            )
            """);
    }

    @Test
    public void create_table_enum_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, EnumModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS enum_model (
                id INTEGER NOT NULL PRIMARY KEY,
                foo INTEGER NOT NULL,
                nested_foo INTEGER NOT NULL,
                nested_s VARCHAR NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, EnumModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS enum_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                foo INTEGER NOT NULL,
                nested_foo INTEGER NOT NULL,
                nested_s VARCHAR NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, EnumModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS enum_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                foo INTEGER NOT NULL,
                nested_foo INTEGER NOT NULL,
                nested_s VARCHAR(4096) NOT NULL
            )
            """);
    }

    @Test
    public void create_table_foreign_key_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, FKIntTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_int (
                id INTEGER NOT NULL PRIMARY KEY,
                value INTEGER NOT NULL
            )
            """);
        assertThat(createTableIfNotExists(Engine.SQLite, FKLongTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_long (
                id INTEGER NOT NULL PRIMARY KEY,
                value INTEGER NOT NULL
            )
            """);
        assertThat(createTableIfNotExists(Engine.SQLite, FKStringTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_string (
                id VARCHAR NOT NULL PRIMARY KEY,
                value VARCHAR NOT NULL
            )
            """);
        assertThat(createTableIfNotExists(Engine.SQLite, ForeignKeyModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_model (
                id INTEGER NOT NULL PRIMARY KEY,
                inner_int_id INTEGER NOT NULL,
                inner_long_id INTEGER NOT NULL,
                inner_string_id VARCHAR NOT NULL,
                FOREIGN KEY(inner_int_id) REFERENCES f_k_int(id),
                FOREIGN KEY(inner_long_id) REFERENCES f_k_long(id),
                FOREIGN KEY(inner_string_id) REFERENCES f_k_string(id)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, FKIntTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_int (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                value INTEGER NOT NULL
            )
            """);
        assertThat(createTableIfNotExists(Engine.MySQL, FKLongTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_long (
                id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                value BIGINT NOT NULL
            )
            """);
        assertThat(createTableIfNotExists(Engine.MySQL, FKStringTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_string (
                id VARCHAR(255) NOT NULL PRIMARY KEY,
                value VARCHAR(4096) NOT NULL
            )
            """);
        assertThat(createTableIfNotExists(Engine.MySQL, ForeignKeyModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_model (
                id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                inner_int_id INTEGER NOT NULL,
                inner_long_id BIGINT NOT NULL,
                inner_string_id VARCHAR(255) NOT NULL,
                FOREIGN KEY(inner_int_id) REFERENCES f_k_int(id),
                FOREIGN KEY(inner_long_id) REFERENCES f_k_long(id),
                FOREIGN KEY(inner_string_id) REFERENCES f_k_string(id)
            )
            """);
    }

    @Test
    public void create_table_foreign_key_nullable_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, ForeignKeyNullableModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_nullable_model (
                id INTEGER NOT NULL PRIMARY KEY,
                inner_int_id INTEGER,
                inner_long_id INTEGER,
                inner_string_id VARCHAR,
                FOREIGN KEY(inner_int_id) REFERENCES f_k_int(id),
                FOREIGN KEY(inner_long_id) REFERENCES f_k_long(id),
                FOREIGN KEY(inner_string_id) REFERENCES f_k_string(id)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, ForeignKeyNullableModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_nullable_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                inner_int_id INTEGER,
                inner_long_id BIGINT,
                inner_string_id VARCHAR(255),
                FOREIGN KEY(inner_int_id) REFERENCES f_k_int(id),
                FOREIGN KEY(inner_long_id) REFERENCES f_k_long(id),
                FOREIGN KEY(inner_string_id) REFERENCES f_k_string(id)
            )
            """);
    }

    @Test
    public void create_table_inherited_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, InheritedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS inherited_model (
                str VARCHAR NOT NULL,
                int_value INTEGER NOT NULL,
                inherited_model_id INTEGER NOT NULL PRIMARY KEY,
                bool_value INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, InheritedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS inherited_model (
                str VARCHAR NOT NULL,
                int_value INTEGER NOT NULL,
                inherited_model_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                bool_value BOOLEAN NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, InheritedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS inherited_model (
                str VARCHAR(4096) NOT NULL,
                int_value INTEGER NOT NULL,
                inherited_model_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                bool_value BOOLEAN NOT NULL
            )
            """);
    }

    @Test
    public void create_table_mapped_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, MapperModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS mapper_model (
                id INTEGER NOT NULL PRIMARY KEY,
                path VARCHAR NOT NULL,
                pair VARCHAR NOT NULL,
                ints BLOB NOT NULL,
                time BLOB NOT NULL,
                bits BLOB NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, MapperModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS mapper_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                path VARCHAR NOT NULL,
                pair VARCHAR NOT NULL,
                ints BLOB NOT NULL,
                time BLOB NOT NULL,
                bits BLOB NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, MapperModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS mapper_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                path VARCHAR(4096) NOT NULL,
                pair VARCHAR(4096) NOT NULL,
                ints BLOB NOT NULL,
                time BLOB NOT NULL,
                bits BLOB NOT NULL
            )
            """);
    }

    @Test
    public void create_table_nested_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, NestedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nested_model (
                id INTEGER NOT NULL PRIMARY KEY,
                simple_id INTEGER NOT NULL,
                simple_a INTEGER NOT NULL,
                simple_b VARCHAR NOT NULL,
                level1_id INTEGER NOT NULL,
                level1_simple_id INTEGER NOT NULL,
                level1_simple_a INTEGER NOT NULL,
                level1_simple_b VARCHAR NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, NestedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nested_model (
                id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                simple_id INTEGER NOT NULL,
                simple_a BIGINT NOT NULL,
                simple_b VARCHAR NOT NULL,
                level1_id INTEGER NOT NULL,
                level1_simple_id INTEGER NOT NULL,
                level1_simple_a BIGINT NOT NULL,
                level1_simple_b VARCHAR NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, NestedModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nested_model (
                id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                simple_id INTEGER NOT NULL,
                simple_a BIGINT NOT NULL,
                simple_b VARCHAR(4096) NOT NULL,
                level1_id INTEGER NOT NULL,
                level1_simple_id INTEGER NOT NULL,
                level1_simple_a BIGINT NOT NULL,
                level1_simple_b VARCHAR(4096) NOT NULL
            )
            """);
    }

    @Test
    public void create_table_nullable_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, NullableModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nullable_model (
                id VARCHAR PRIMARY KEY,
                str VARCHAR,
                timestamp INTEGER,
                ch VARCHAR,
                nested_id INTEGER,
                nested_s VARCHAR
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, NullableModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nullable_model (
                id VARCHAR PRIMARY KEY,
                str VARCHAR,
                timestamp TIMESTAMP,
                ch VARCHAR,
                nested_id INTEGER,
                nested_s VARCHAR
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, NullableModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nullable_model (
                id VARCHAR(255) PRIMARY KEY,
                str VARCHAR(4096),
                timestamp TIMESTAMP(3),
                ch VARCHAR(4096),
                nested_id INTEGER,
                nested_s VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_optional_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, OptionalModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS optional_model (
                id INTEGER NOT NULL PRIMARY KEY,
                i INTEGER,
                l INTEGER,
                str VARCHAR
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, OptionalModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS optional_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                str VARCHAR
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, OptionalModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS optional_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                str VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_pojo_with_adapter_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, PojoWithAdapterModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER NOT NULL PRIMARY KEY,
                pojo_id INTEGER NOT NULL,
                pojo_buf VARCHAR NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, PojoWithAdapterModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                pojo_id INTEGER NOT NULL,
                pojo_buf VARCHAR NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, PojoWithAdapterModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                pojo_id INTEGER NOT NULL,
                pojo_buf VARCHAR(4096) NOT NULL
            )
            """);
    }

    @Test
    public void create_table_pojo_with_mapper_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, PojoWithMapperModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_mapper_model (
                id INTEGER NOT NULL PRIMARY KEY,
                pojo_coordinates INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, PojoWithMapperModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_mapper_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                pojo_coordinates BIGINT NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, PojoWithMapperModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_mapper_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                pojo_coordinates BIGINT NOT NULL
            )
            """);
    }

    @Test
    public void create_table_primitive_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, PrimitiveModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS primitive_model (
                id INTEGER NOT NULL PRIMARY KEY,
                i INTEGER NOT NULL,
                l INTEGER NOT NULL,
                b INTEGER NOT NULL,
                s INTEGER NOT NULL,
                ch VARCHAR NOT NULL,
                f REAL NOT NULL,
                d REAL NOT NULL,
                bool INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, PrimitiveModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS primitive_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b TINYINT NOT NULL,
                s SMALLINT NOT NULL,
                ch VARCHAR NOT NULL,
                f REAL NOT NULL,
                d DOUBLE NOT NULL,
                bool BOOLEAN NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, PrimitiveModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS primitive_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b TINYINT NOT NULL,
                s SMALLINT NOT NULL,
                ch VARCHAR(4096) NOT NULL,
                f REAL NOT NULL,
                d DOUBLE NOT NULL,
                bool BOOLEAN NOT NULL
            )
            """);
    }

    @Test
    public void create_table_string_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, StringModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR NOT NULL PRIMARY KEY,
                sequence VARCHAR NOT NULL,
                chars VARCHAR NOT NULL,
                raw_bytes BLOB NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, StringModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR NOT NULL PRIMARY KEY,
                sequence VARCHAR NOT NULL,
                chars VARCHAR NOT NULL,
                raw_bytes BLOB NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, StringModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR(255) NOT NULL PRIMARY KEY,
                sequence VARCHAR(4096) NOT NULL,
                chars VARCHAR(4096) NOT NULL,
                raw_bytes BLOB NOT NULL
            )
            """);
    }

    @Test
    public void create_table_timing_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, TimingModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS timing_model (
                id INTEGER NOT NULL PRIMARY KEY,
                util_date INTEGER NOT NULL,
                sql_date INTEGER NOT NULL,
                time INTEGER NOT NULL,
                instant INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                local_date INTEGER NOT NULL,
                local_time INTEGER NOT NULL,
                local_date_time INTEGER NOT NULL,
                zoned_date_time INTEGER NOT NULL,
                offset_time_time INTEGER NOT NULL,
                offset_time_zone_offset_seconds INTEGER NOT NULL,
                offset_date_time_timestamp INTEGER NOT NULL,
                offset_date_time_zone_offset_seconds INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                period VARCHAR NOT NULL,
                zone_offset INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, TimingModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS timing_model (
                id TIMESTAMP NOT NULL PRIMARY KEY,
                util_date DATE NOT NULL,
                sql_date DATE NOT NULL,
                time TIME NOT NULL,
                instant TIMESTAMP NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                local_date DATE NOT NULL,
                local_time TIME NOT NULL,
                local_date_time TIMESTAMP NOT NULL,
                zoned_date_time TIMESTAMP NOT NULL,
                offset_time_time TIME NOT NULL,
                offset_time_zone_offset_seconds INTEGER NOT NULL,
                offset_date_time_timestamp TIMESTAMP NOT NULL,
                offset_date_time_zone_offset_seconds INTEGER NOT NULL,
                duration BIGINT NOT NULL,
                period VARCHAR NOT NULL,
                zone_offset INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, TimingModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS timing_model (
                id TIMESTAMP(3) NOT NULL PRIMARY KEY,
                util_date DATE NOT NULL,
                sql_date DATE NOT NULL,
                time TIME NOT NULL,
                instant TIMESTAMP(3) NOT NULL,
                timestamp TIMESTAMP(3) NOT NULL,
                local_date DATE NOT NULL,
                local_time TIME NOT NULL,
                local_date_time TIMESTAMP(3) NOT NULL,
                zoned_date_time TIMESTAMP(3) NOT NULL,
                offset_time_time TIME NOT NULL,
                offset_time_zone_offset_seconds INTEGER NOT NULL,
                offset_date_time_timestamp TIMESTAMP(3) NOT NULL,
                offset_date_time_zone_offset_seconds INTEGER NOT NULL,
                duration BIGINT NOT NULL,
                period VARCHAR(4096) NOT NULL,
                zone_offset INTEGER NOT NULL
            )
            """);
    }

    @Test
    public void create_table_wrappers_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, WrappersModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS wrappers_model (
                id INTEGER NOT NULL PRIMARY KEY,
                i INTEGER NOT NULL,
                l INTEGER NOT NULL,
                b INTEGER NOT NULL,
                s INTEGER NOT NULL,
                ch VARCHAR NOT NULL,
                f REAL NOT NULL,
                d REAL NOT NULL,
                bool INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, WrappersModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS wrappers_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b TINYINT NOT NULL,
                s SMALLINT NOT NULL,
                ch VARCHAR NOT NULL,
                f REAL NOT NULL,
                d DOUBLE NOT NULL,
                bool BOOLEAN NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, WrappersModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS wrappers_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b TINYINT NOT NULL,
                s SMALLINT NOT NULL,
                ch VARCHAR(4096) NOT NULL,
                f REAL NOT NULL,
                d DOUBLE NOT NULL,
                bool BOOLEAN NOT NULL
            )
            """);
    }

    @Test
    public void create_table_bridge_int_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, BridgeIntModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS bridge_int_model (
                foo_id INTEGER NOT NULL,
                bar_id INTEGER NOT NULL,
                FOREIGN KEY(foo_id) REFERENCES user(user_id),
                FOREIGN KEY(bar_id) REFERENCES user(user_id)
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, BridgeIntModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS bridge_int_model (
                foo_id INTEGER NOT NULL,
                bar_id INTEGER NOT NULL,
                FOREIGN KEY(foo_id) REFERENCES user(user_id),
                FOREIGN KEY(bar_id) REFERENCES user(user_id)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, BridgeIntModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS bridge_int_model (
                foo_id INTEGER NOT NULL,
                bar_id INTEGER NOT NULL,
                FOREIGN KEY(foo_id) REFERENCES user(user_id),
                FOREIGN KEY(bar_id) REFERENCES user(user_id)
            )
            """);
    }

    @Test
    public void create_table_ints_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, IntsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS ints_model (
                foo INTEGER NOT NULL DEFAULT (0),
                bar INTEGER NOT NULL DEFAULT (0),
                value INTEGER NOT NULL DEFAULT (0)
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, IntsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS ints_model (
                foo INTEGER NOT NULL DEFAULT (0),
                bar INTEGER NOT NULL DEFAULT (0),
                value INTEGER NOT NULL DEFAULT (0)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, IntsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS ints_model (
                foo INTEGER NOT NULL DEFAULT (0),
                bar INTEGER NOT NULL DEFAULT (0),
                value INTEGER NOT NULL DEFAULT (0)
            )
            """);
    }

    @Test
    public void create_table_longs_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, LongsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS longs_model (
                foo INTEGER NOT NULL DEFAULT (0),
                bar INTEGER NOT NULL DEFAULT (0),
                value INTEGER NOT NULL DEFAULT (0)
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, LongsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS longs_model (
                foo BIGINT NOT NULL DEFAULT (0),
                bar BIGINT NOT NULL DEFAULT (0),
                value BIGINT NOT NULL DEFAULT (0)
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, LongsModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS longs_model (
                foo BIGINT NOT NULL DEFAULT (0),
                bar BIGINT NOT NULL DEFAULT (0),
                value BIGINT NOT NULL DEFAULT (0)
            )
            """);
    }

    @Test
    public void create_table_user_rate_model() {
        assertThat(createTableIfNotExists(Engine.SQLite, UserRateModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user_rate_model (
                user_id INTEGER NOT NULL,
                content_id INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.H2, UserRateModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user_rate_model (
                user_id INTEGER NOT NULL,
                content_id INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);

        assertThat(createTableIfNotExists(Engine.MySQL, UserRateModelTable.META)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user_rate_model (
                user_id INTEGER NOT NULL,
                content_id INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);
    }

    private static @NotNull String createTableIfNotExists(@NotNull Engine engine, @NotNull TableMeta meta) {
        return CreateTableQuery.of(meta).ifNotExists().build(engine).repr();
    }
}
