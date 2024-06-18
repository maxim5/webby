package io.webby.db.kv.impl;

import com.google.common.flogger.FluentLogger;
import io.webby.db.kv.DbType;
import io.webby.util.base.Pair;
import io.webby.util.classpath.EasyClasspath;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class KeyValueDbTypeDetector {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final List<Pair<DbType, String>> AUTO_TYPES = List.of(
        Pair.of(DbType.CHRONICLE_MAP, "net.openhft.chronicle.map.ChronicleMapBuilder"),
        Pair.of(DbType.HALO_DB, "com.oath.halodb.HaloDB"),
        Pair.of(DbType.LEVEL_DB_IQ80, "org.iq80.leveldb.impl.Iq80DBFactory"),
        Pair.of(DbType.LEVEL_DB_JNI, "org.fusesource.leveldbjni.JniDBFactory"),
        Pair.of(DbType.LMDB_JAVA, "org.lmdbjava.Dbi"),
        Pair.of(DbType.LMDB_JNI, "org.fusesource.lmdbjni.Database"),
        Pair.of(DbType.MAP_DB, "org.mapdb.DB"),
        Pair.of(DbType.OAK, "com.yahoo.oak.OakMap"),
        Pair.of(DbType.ROCKS_DB, "org.rocksdb.RocksDB"),
        Pair.of(DbType.SWAY_DB, "swaydb.java.Map"),
        Pair.of(DbType.TUPL, "org.cojen.tupl.Database")

        // Exclude from auto-detection, should be set explicitly:
        // Pair.of(DbType.JEDIS, "redis.clients.jedis.Jedis"),
        // Pair.of(DbType.PAL_DB, "com.linkedin.paldb.api.PalDB")
    );

    public static @Nullable DbType autoDetectDbTypeFromClasspath() {
        for (Pair<DbType, String> val : AUTO_TYPES) {
            if (EasyClasspath.isInClassPath(val.second())) {
                log.at(Level.INFO).log("Found key-value persistence library in classpath: %s", val.second());
                return val.first();
            }
        }
        return null;
    }
}
