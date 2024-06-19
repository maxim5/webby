package io.spbx.webby.db.kv.redis;

import com.google.inject.Inject;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

public class JedisDbFactory extends BaseKeyValueFactory {
    @Inject private RedisSettings redisSettings;

    @Override
    public @NotNull <K, V> JedisDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);

            HostAndPort hp = new HostAndPort(redisSettings.host(), redisSettings.port());
            DefaultJedisClientConfig config = DefaultJedisClientConfig.builder()
                .user(redisSettings.user())
                .password(redisSettings.password())
                .database(redisSettings.database())
                .ssl(redisSettings.ssl())
                .build();
            Jedis jedis = new Jedis(hp, config);

            return new JedisDb<>(jedis, options.name(), keyCodec, valueCodec);
        });
    }
}
