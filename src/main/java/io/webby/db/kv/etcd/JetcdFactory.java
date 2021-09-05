package io.webby.db.kv.etcd;

import com.google.inject.Inject;
import io.etcd.jetcd.Client;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

public class JetcdFactory extends BaseKeyValueFactory {
    @Inject private EtcdSettings etcdSettings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> JetcdDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);
            Client client = Client.builder().endpoints(etcdSettings.endpoint()).build();
            return new JetcdDb<>(client.getKVClient(), name, keyCodec, valueCodec);
        });
    }
}
