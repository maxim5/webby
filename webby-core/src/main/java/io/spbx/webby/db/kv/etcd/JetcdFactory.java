package io.spbx.webby.db.kv.etcd;

import com.google.inject.Inject;
import io.etcd.jetcd.Client;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

public class JetcdFactory extends BaseKeyValueFactory {
    @Inject private EtcdSettings etcdSettings;

    @Override
    public @NotNull <K, V> JetcdDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);
            Client client = Client.builder().endpoints(etcdSettings.endpoint()).build();
            return new JetcdDb<>(client.getKVClient(), options.name(), keyCodec, valueCodec);
        });
    }
}
