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
                user_id INTEGER PRIMARY KEY,
                created_at INTEGER,
                access_level INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, UserTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                created_at TIMESTAMP,
                access_level INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, UserTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS user (
                user_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                created_at TIMESTAMP(3),
                access_level INTEGER
            )
            """);
    }

    @Test
    public void create_table_session() {
        assertThat(makeCreateTableQuery(Engine.SQLite, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id INTEGER PRIMARY KEY,
                user_id INTEGER,
                created_at INTEGER,
                user_agent VARCHAR,
                ip_address VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id INTEGER,
                created_at TIMESTAMP,
                user_agent VARCHAR,
                ip_address VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, SessionTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS session (
                session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id INTEGER,
                created_at TIMESTAMP(3),
                user_agent VARCHAR(4096),
                ip_address VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_blob_kv() {
        assertThat(makeCreateTableQuery(Engine.SQLite, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR PRIMARY KEY,
                value BLOB
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARCHAR PRIMARY KEY,
                value BLOB
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, BlobKvTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS blob_kv (
                id VARBINARY(255) PRIMARY KEY,
                value BLOB
            )
            """);
    }

    // Example models

    @Test
    public void create_table_atomic_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, AtomicModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER PRIMARY KEY,
                i INTEGER,
                l INTEGER,
                b INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, AtomicModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                b BOOLEAN
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, AtomicModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS atomic_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                b BOOLEAN
            )
            """);
    }

    @Test
    public void create_table_complex_id_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, ComplexIdModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS complex_id_model (
                id_x INTEGER,
                id_y INTEGER,
                id_z VARCHAR,
                a INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, ComplexIdModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS complex_id_model (
                id_x INTEGER,
                id_y BIGINT,
                id_z VARCHAR(4096),
                a INTEGER
            )
            """);
    }

    @Test
    public void create_table_deep_nested_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, DeepNestedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS deep_nested_model (
                id INTEGER PRIMARY KEY,
                d_id INTEGER,
                d_c_id INTEGER,
                d_c_a_id INTEGER,
                d_c_b_id INTEGER,
                d_c_b_a_id INTEGER,
                d_c_aa_id INTEGER,
                d_b_id INTEGER,
                d_b_a_id INTEGER,
                a_id INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, DeepNestedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS deep_nested_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                d_id INTEGER,
                d_c_id INTEGER,
                d_c_a_id INTEGER,
                d_c_b_id INTEGER,
                d_c_b_a_id INTEGER,
                d_c_aa_id INTEGER,
                d_b_id INTEGER,
                d_b_a_id INTEGER,
                a_id INTEGER
            )
            """);
    }

    @Test
    public void create_table_enum_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, EnumModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS enum_model (
                id_ord INTEGER PRIMARY KEY,
                foo_ord INTEGER,
                nested_foo_ord INTEGER,
                nested_s VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, EnumModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS enum_model (
                id_ord INTEGER PRIMARY KEY AUTO_INCREMENT,
                foo_ord INTEGER,
                nested_foo_ord INTEGER,
                nested_s VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_foreign_key_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, FKIntTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_int (
                id INTEGER PRIMARY KEY,
                value INTEGER
            )
            """);
        assertThat(makeCreateTableQuery(Engine.SQLite, FKLongTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_long (
                id INTEGER PRIMARY KEY,
                value INTEGER
            )
            """);
        assertThat(makeCreateTableQuery(Engine.SQLite, FKStringTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_string (
                id VARCHAR PRIMARY KEY,
                value VARCHAR
            )
            """);
        assertThat(makeCreateTableQuery(Engine.SQLite, ForeignKeyModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_model (
                id INTEGER PRIMARY KEY,
                inner_int_id INTEGER,
                inner_long_id INTEGER,
                inner_string_id VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, FKIntTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_int (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                value INTEGER
            )
            """);
        assertThat(makeCreateTableQuery(Engine.MySQL, FKLongTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_long (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                value BIGINT
            )
            """);
        assertThat(makeCreateTableQuery(Engine.MySQL, FKStringTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS f_k_string (
                id VARCHAR(255) PRIMARY KEY,
                value VARCHAR(4096)
            )
            """);
        assertThat(makeCreateTableQuery(Engine.MySQL, ForeignKeyModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS foreign_key_model (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                inner_int_id INTEGER,
                inner_long_id BIGINT,
                inner_string_id VARCHAR(255)
            )
            """);
    }

    @Test
    public void create_table_inherited_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, InheritedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS inherited_model (
                str VARCHAR,
                int_value INTEGER,
                inherited_model_id INTEGER PRIMARY KEY,
                bool_value INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, InheritedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS inherited_model (
                str VARCHAR(4096),
                int_value INTEGER,
                inherited_model_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                bool_value BOOLEAN
            )
            """);
    }

    @Test
    public void create_table_nested_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, NestedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nested_model (
                id INTEGER PRIMARY KEY,
                simple_id INTEGER,
                simple_a INTEGER,
                simple_b VARCHAR,
                level1_id INTEGER,
                level1_simple_id INTEGER,
                level1_simple_a INTEGER,
                level1_simple_b VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, NestedModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nested_model (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                simple_id INTEGER,
                simple_a BIGINT,
                simple_b VARCHAR(4096),
                level1_id INTEGER,
                level1_simple_id INTEGER,
                level1_simple_a BIGINT,
                level1_simple_b VARCHAR(4096)
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
                nested_id INTEGER,
                nested_s VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, NullableModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nullable_model (
                id VARCHAR PRIMARY KEY,
                str VARCHAR,
                timestamp TIMESTAMP,
                nested_id INTEGER,
                nested_s VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, NullableModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS nullable_model (
                id VARCHAR(255) PRIMARY KEY,
                str VARCHAR(4096),
                timestamp TIMESTAMP(3),
                nested_id INTEGER,
                nested_s VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_pojo_with_adapter_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, PojoWithAdapterModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER PRIMARY KEY,
                pojo_id INTEGER,
                pojo_buf VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, PojoWithAdapterModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                pojo_id INTEGER,
                pojo_buf VARCHAR
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, PojoWithAdapterModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS pojo_with_adapter_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                pojo_id INTEGER,
                pojo_buf VARCHAR(4096)
            )
            """);
    }

    @Test
    public void create_table_primitive_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, PrimitiveModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS primitive_model (
                id INTEGER PRIMARY KEY,
                i INTEGER,
                l INTEGER,
                b INTEGER,
                s INTEGER,
                ch VARCHAR,
                f REAL,
                d REAL,
                bool INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, PrimitiveModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS primitive_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                b TINYINT,
                s SMALLINT,
                ch VARCHAR,
                f REAL,
                d DOUBLE,
                bool BOOLEAN
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, PrimitiveModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS primitive_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                b TINYINT,
                s SMALLINT,
                ch VARCHAR(4096),
                f REAL,
                d DOUBLE,
                bool BOOLEAN
            )
            """);
    }

    @Test
    public void create_table_string_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, StringModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR PRIMARY KEY,
                sequence VARCHAR,
                chars VARCHAR,
                raw_bytes BLOB
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, StringModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR PRIMARY KEY,
                sequence VARCHAR,
                chars VARCHAR,
                raw_bytes BLOB
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, StringModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS string_model (
                id VARCHAR(255) PRIMARY KEY,
                sequence VARCHAR(4096),
                chars VARCHAR(4096),
                raw_bytes BLOB
            )
            """);
    }

    @Test
    public void create_table_timing_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, TimingModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS timing_model (
                id INTEGER PRIMARY KEY,
                util_date INTEGER,
                sql_date INTEGER,
                time INTEGER,
                instant INTEGER,
                timestamp INTEGER,
                local_date INTEGER,
                local_time INTEGER,
                local_date_time INTEGER,
                zoned_date_time INTEGER,
                offset_time_time INTEGER,
                offset_time_zone_offset_seconds INTEGER,
                offset_date_time_timestamp INTEGER,
                offset_date_time_zone_offset_seconds INTEGER,
                duration INTEGER,
                period VARCHAR,
                zone_offset INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, TimingModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS timing_model (
                id TIMESTAMP PRIMARY KEY,
                util_date DATE,
                sql_date DATE,
                time TIME,
                instant TIMESTAMP,
                timestamp TIMESTAMP,
                local_date DATE,
                local_time TIME,
                local_date_time TIMESTAMP,
                zoned_date_time TIMESTAMP,
                offset_time_time TIME,
                offset_time_zone_offset_seconds INTEGER,
                offset_date_time_timestamp TIMESTAMP,
                offset_date_time_zone_offset_seconds INTEGER,
                duration BIGINT,
                period VARCHAR,
                zone_offset INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, TimingModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS timing_model (
                id TIMESTAMP(3) PRIMARY KEY,
                util_date DATE,
                sql_date DATE,
                time TIME,
                instant TIMESTAMP(3),
                timestamp TIMESTAMP(3),
                local_date DATE,
                local_time TIME,
                local_date_time TIMESTAMP(3),
                zoned_date_time TIMESTAMP(3),
                offset_time_time TIME,
                offset_time_zone_offset_seconds INTEGER,
                offset_date_time_timestamp TIMESTAMP(3),
                offset_date_time_zone_offset_seconds INTEGER,
                duration BIGINT,
                period VARCHAR(4096),
                zone_offset INTEGER
            )
            """);
    }

    @Test
    public void create_table_wrappers_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, WrappersModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS wrappers_model (
                id INTEGER PRIMARY KEY,
                i INTEGER,
                l INTEGER,
                b INTEGER,
                s INTEGER,
                ch VARCHAR,
                f REAL,
                d REAL,
                bool INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, WrappersModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS wrappers_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                b TINYINT,
                s SMALLINT,
                ch VARCHAR,
                f REAL,
                d DOUBLE,
                bool BOOLEAN
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, WrappersModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS wrappers_model (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                i INTEGER,
                l BIGINT,
                b TINYINT,
                s SMALLINT,
                ch VARCHAR(4096),
                f REAL,
                d DOUBLE,
                bool BOOLEAN
            )
            """);
    }

    @Test
    public void create_table_m2m_int_model() {
        assertThat(makeCreateTableQuery(Engine.SQLite, M2mIntModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS m2m_int_model (
                foo_id INTEGER,
                bar_id INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.H2, M2mIntModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS m2m_int_model (
                foo_id INTEGER,
                bar_id INTEGER
            )
            """);

        assertThat(makeCreateTableQuery(Engine.MySQL, M2mIntModelTable.class)).isEqualTo("""
            CREATE TABLE IF NOT EXISTS m2m_int_model (
                foo_id INTEGER,
                bar_id INTEGER
            )
            """);
    }
}
