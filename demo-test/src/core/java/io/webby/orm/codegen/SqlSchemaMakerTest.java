package io.webby.orm.codegen;

import io.webby.auth.session.SessionTable;
import io.webby.auth.user.UserTable;
import io.webby.db.model.BlobKvTable;
import io.webby.demo.model.*;
import io.webby.orm.api.Engine;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.codegen.SqlSchemaMaker.makeCreateTableQuery;

public class SqlSchemaMakerTest {
    @Test
    public void create_table_user() {
        assertThat(makeCreateTableQuery(Engine.SQLite, UserTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER NOT NULL PRIMARY KEY,
                created_at INTEGER NOT NULL,
                access_level INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, UserTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                created_at TIMESTAMP NOT NULL,
                access_level INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, UserTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                created_at TIMESTAMP(3) NOT NULL,
                access_level INTEGER NOT NULL
            )
            """);
    }

    @Test
    public void create_table_session() {
        assertThat(makeCreateTableQuery(Engine.SQLite, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id INTEGER NOT NULL PRIMARY KEY,
                user_id INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                user_agent VARCHAR NOT NULL,
                ip_address VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                user_id INTEGER NOT NULL,
                created_at TIMESTAMP NOT NULL,
                user_agent VARCHAR NOT NULL,
                ip_address VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                user_id INTEGER NOT NULL,
                created_at TIMESTAMP(3) NOT NULL,
                user_agent VARCHAR(4096) NOT NULL,
                ip_address VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_blob_kv() {
        assertThat(makeCreateTableQuery(Engine.SQLite, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR NOT NULL PRIMARY KEY,
                value BLOB
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR NOT NULL PRIMARY KEY,
                value BLOB
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARBINARY(255) NOT NULL PRIMARY KEY,
                value BLOB
            )
            """);
    }

    // Example models

    @Test
    public void create_table_atomic_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, AtomicModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER NOT NULL PRIMARY KEY,
                i INTEGER NOT NULL,
                l INTEGER NOT NULL,
                b INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, AtomicModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b BOOLEAN NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, AtomicModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                i INTEGER NOT NULL,
                l BIGINT NOT NULL,
                b BOOLEAN NOT NULL
            )
            """);
    }

    @Test
    public void create_table_complex_id_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, ComplexIdModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS complex_id_model (
                id_x INTEGER NOT NULL,
                id_y INTEGER NOT NULL,
                id_z VARCHAR NOT NULL,
                a INTEGER NOT NULL,
                PRIMARY KEY (id_x, id_y, id_z)
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, ComplexIdModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS complex_id_model (
                id_x INTEGER NOT NULL,
                id_y BIGINT NOT NULL,
                id_z VARCHAR NOT NULL,
                a INTEGER NOT NULL,
                PRIMARY KEY (id_x, id_y, id_z)
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, ComplexIdModelTable.class)).isEqualTo("""
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
        assertThat(makeCreateTableQuery(Engine.SQLite, ConstraintsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS constraints_model (
                key_id INTEGER NOT NULL PRIMARY KEY,
                fprint INTEGER NOT NULL UNIQUE,
                range_from INTEGER NOT NULL,
                range_to INTEGER NOT NULL,
                name VARCHAR NOT NULL,
                UNIQUE (range_from, range_to)
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, ConstraintsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS constraints_model (
                key_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                fprint INTEGER NOT NULL UNIQUE,
                range_from INTEGER NOT NULL,
                range_to INTEGER NOT NULL,
                name VARCHAR NOT NULL,
                UNIQUE (range_from, range_to)
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, ConstraintsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS constraints_model (
                key_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                fprint INTEGER NOT NULL UNIQUE,
                range_from INTEGER NOT NULL,
                range_to INTEGER NOT NULL,
                name VARCHAR(4096) NOT NULL,
                UNIQUE (range_from, range_to)
            )
            """);
    }

    @Test
    public void create_table_deep_nested_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, DeepNestedModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.MySQL, DeepNestedModelTable.class)).isEqualTo("""
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
        assertThat(makeCreateTableQuery(Engine.SQLite, EnumModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS enum_model (
                id_ord INTEGER NOT NULL PRIMARY KEY,
                foo_ord INTEGER NOT NULL,
                nested_foo_ord INTEGER NOT NULL,
                nested_s VARCHAR NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, EnumModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS enum_model (
                id_ord INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                foo_ord INTEGER NOT NULL,
                nested_foo_ord INTEGER NOT NULL,
                nested_s VARCHAR(4096) NOT NULL
            )
            """);
    }

    @Test
    public void create_table_foreign_key_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, FKIntTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_int (
                id INTEGER NOT NULL PRIMARY KEY,
                value INTEGER NOT NULL
            )
            """);
        assertThat(makeCreateTableQuery(Engine.SQLite, FKLongTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_long (
                id INTEGER NOT NULL PRIMARY KEY,
                value INTEGER NOT NULL
            )
            """);
        assertThat(makeCreateTableQuery(Engine.SQLite, FKStringTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_string (
                id VARCHAR NOT NULL PRIMARY KEY,
                value VARCHAR NOT NULL
            )
            """);
        assertThat(makeCreateTableQuery(Engine.SQLite, ForeignKeyModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_model (
                id INTEGER NOT NULL PRIMARY KEY,
                inner_int_id INTEGER NOT NULL,
                inner_long_id INTEGER NOT NULL,
                inner_string_id VARCHAR NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, FKIntTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_int (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                value INTEGER NOT NULL
            )
            """);
        assertThat(makeCreateTableQuery(Engine.MySQL, FKLongTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_long (
                id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                value BIGINT NOT NULL
            )
            """);
        assertThat(makeCreateTableQuery(Engine.MySQL, FKStringTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_string (
                id VARCHAR(255) NOT NULL PRIMARY KEY,
                value VARCHAR(4096) NOT NULL
            )
            """);
        assertThat(makeCreateTableQuery(Engine.MySQL, ForeignKeyModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_model (
                id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                inner_int_id INTEGER NOT NULL,
                inner_long_id BIGINT NOT NULL,
                inner_string_id VARCHAR(255) NOT NULL
            )
            """);
    }

    @Test
    public void create_table_inherited_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, InheritedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS inherited_model (
                str VARCHAR NOT NULL,
                int_value INTEGER NOT NULL,
                inherited_model_id INTEGER NOT NULL PRIMARY KEY,
                bool_value INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, InheritedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS inherited_model (
                str VARCHAR(4096) NOT NULL,
                int_value INTEGER NOT NULL,
                inherited_model_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                bool_value BOOLEAN NOT NULL
            )
            """);
    }

    @Test
    public void create_table_nested_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, NestedModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.MySQL, NestedModelTable.class)).isEqualTo("""
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
        assertThat(makeCreateTableQuery(Engine.SQLite, NullableModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nullable_model (
                id VARCHAR PRIMARY KEY,
                str VARCHAR,
                timestamp INTEGER,
                ch VARCHAR,
                nested_id INTEGER,
                nested_s VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, NullableModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nullable_model (
                id VARCHAR PRIMARY KEY,
                str VARCHAR,
                timestamp TIMESTAMP,
                ch VARCHAR,
                nested_id INTEGER,
                nested_s VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, NullableModelTable.class)).isEqualTo("""
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
    public void create_table_pojo_with_adapter_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, PojoWithAdapterModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER NOT NULL PRIMARY KEY,
                pojo_id INTEGER NOT NULL,
                pojo_buf VARCHAR NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, PojoWithAdapterModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                pojo_id INTEGER NOT NULL,
                pojo_buf VARCHAR NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, PojoWithAdapterModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
                pojo_id INTEGER NOT NULL,
                pojo_buf VARCHAR(4096) NOT NULL
            )
            """);
    }

    @Test
    public void create_table_primitive_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, PrimitiveModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.H2, PrimitiveModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.MySQL, PrimitiveModelTable.class)).isEqualTo("""
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
        assertThat(makeCreateTableQuery(Engine.SQLite, StringModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR NOT NULL PRIMARY KEY,
                sequence VARCHAR NOT NULL,
                chars VARCHAR NOT NULL,
                raw_bytes BLOB NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, StringModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR NOT NULL PRIMARY KEY,
                sequence VARCHAR NOT NULL,
                chars VARCHAR NOT NULL,
                raw_bytes BLOB NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, StringModelTable.class)).isEqualTo("""
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
        assertThat(makeCreateTableQuery(Engine.SQLite, TimingModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.H2, TimingModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.MySQL, TimingModelTable.class)).isEqualTo("""
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
        assertThat(makeCreateTableQuery(Engine.SQLite, WrappersModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.H2, WrappersModelTable.class)).isEqualTo("""
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

        assertThat(makeCreateTableQuery(Engine.MySQL, WrappersModelTable.class)).isEqualTo("""
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
    public void create_table_m2m_int_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, M2mIntModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS m2m_int_model (
                foo_id INTEGER NOT NULL,
                bar_id INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, M2mIntModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS m2m_int_model (
                foo_id INTEGER NOT NULL,
                bar_id INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, M2mIntModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS m2m_int_model (
                foo_id INTEGER NOT NULL,
                bar_id INTEGER NOT NULL
            )
            """);
    }

    @Test
    public void create_table_ints_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, IntsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS ints_model (
                foo INTEGER NOT NULL,
                bar INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, IntsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS ints_model (
                foo INTEGER NOT NULL,
                bar INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, IntsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS ints_model (
                foo INTEGER NOT NULL,
                bar INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);
    }

    @Test
    public void create_table_longs_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, LongsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS longs_model (
                foo INTEGER NOT NULL,
                bar INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, LongsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS longs_model (
                foo BIGINT NOT NULL,
                bar BIGINT NOT NULL,
                value BIGINT NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, LongsModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS longs_model (
                foo BIGINT NOT NULL,
                bar BIGINT NOT NULL,
                value BIGINT NOT NULL
            )
            """);
    }

    @Test
    public void create_table_user_rate_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, UserRateModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user_rate_model (
                user_id INTEGER NOT NULL,
                content_id INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, UserRateModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user_rate_model (
                user_id INTEGER NOT NULL,
                content_id INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, UserRateModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user_rate_model (
                user_id INTEGER NOT NULL,
                content_id INTEGER NOT NULL,
                value INTEGER NOT NULL
            )
            """);
    }
}
