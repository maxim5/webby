package io.webby.db.kv;

public enum StorageType {
    CHRONICLE_MAP,
    HALO_DB,
    JAVA_MAP,
    JEDIS,
    LEVEL_DB_IQ80,
    LEVEL_DB_JNI,
    LMDB_JAVA,
    LMDB_JNI,
    MAP_DB,
    OAK,
    PAL_DB,
    ROCKS_DB,
    SQL_DB,
    SWAY_DB,
    TUPL;

    public boolean isPersisted() {
        return this != JAVA_MAP && this != SQL_DB;
    }
}
