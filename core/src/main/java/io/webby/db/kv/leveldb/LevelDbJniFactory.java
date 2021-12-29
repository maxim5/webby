package io.webby.db.kv.leveldb;

import org.fusesource.leveldbjni.JniDBFactory;

public class LevelDbJniFactory extends BaseLevelDbFactory {
    public LevelDbJniFactory() {
        super(JniDBFactory.factory);
    }
}
