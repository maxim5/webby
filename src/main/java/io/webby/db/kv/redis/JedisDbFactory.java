package io.webby.db.kv.redis;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

public class JedisDbFactory extends BaseKeyValueFactory {
    // private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private RedisSettings redisSettings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> JedisDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);

            HostAndPort hp = new HostAndPort(redisSettings.host(), redisSettings.port());
            DefaultJedisClientConfig config = DefaultJedisClientConfig.builder()
                    .user(redisSettings.user())
                    .password(redisSettings.password())
                    .database(redisSettings.database())
                    .ssl(redisSettings.ssl())
                    .build();
            Jedis jedis = new Jedis(hp, config);

            return new JedisDb<>(jedis, name, keyCodec, valueCodec);
        });
    }
}
