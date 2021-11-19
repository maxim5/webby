package io.webby.db.kv.impl;

import com.google.common.flogger.FluentLogger;
import io.webby.db.kv.StorageType;
import io.webby.util.collect.Pair;
import io.webby.util.reflect.EasyClasspath;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class KeyValueStorageTypeDetector {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final List<Pair<StorageType, String>> AUTO_TYPES = List.of(
        Pair.of(StorageType.CHRONICLE_MAP, "net.openhft.chronicle.map.ChronicleMapBuilder"),
        Pair.of(StorageType.HALO_DB, "com.oath.halodb.HaloDB"),
        Pair.of(StorageType.LEVEL_DB_IQ80, "org.iq80.leveldb.impl.Iq80DBFactory"),
        Pair.of(StorageType.LEVEL_DB_JNI, "org.fusesource.leveldbjni.JniDBFactory"),
        Pair.of(StorageType.LMDB_JAVA, "org.lmdbjava.Dbi"),
        Pair.of(StorageType.LMDB_JNI, "org.fusesource.lmdbjni.Database"),
        Pair.of(StorageType.MAP_DB, "org.mapdb.DB"),
        Pair.of(StorageType.OAK, "com.yahoo.oak.OakMap"),
        Pair.of(StorageType.ROCKS_DB, "org.rocksdb.RocksDB"),
        Pair.of(StorageType.SWAY_DB, "swaydb.java.Map"),
        Pair.of(StorageType.TUPL, "org.cojen.tupl.Database")

        // Exclude from auto-detection, should be set explicitly:
        // Pair.of(StorageType.JEDIS, "redis.clients.jedis.Jedis"),
        // Pair.of(StorageType.PAL_DB, "com.linkedin.paldb.api.PalDB")
    );

    public static @Nullable StorageType autoDetectStorageTypeFromClasspath() {
        for (Pair<StorageType, String> val : AUTO_TYPES) {
            if (EasyClasspath.isInClassPath(val.second())) {
                log.at(Level.INFO).log("Found key-value persistence library in classpath: %s", val.second());
                return val.first();
            }
        }
        return null;
    }
}
