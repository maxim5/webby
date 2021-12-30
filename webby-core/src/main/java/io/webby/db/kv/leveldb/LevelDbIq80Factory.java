package io.webby.db.kv.leveldb;

import org.iq80.leveldb.impl.Iq80DBFactory;

public class LevelDbIq80Factory extends BaseLevelDbFactory {
    public LevelDbIq80Factory() {
        super(Iq80DBFactory.factory);
    }
}
