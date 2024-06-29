package io.spbx.webby.db.codec;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import io.spbx.util.base.Int128;
import io.spbx.util.testing.ext.HppcBytecodeExtension;
import io.spbx.webby.auth.session.DefaultSession;
import io.spbx.webby.auth.user.DefaultUser;
import io.spbx.webby.testing.SessionBuilder;
import io.spbx.webby.testing.Testing;
import io.spbx.webby.testing.UserBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.spbx.webby.db.codec.AssertCodec.assertCodec;

public class CodecProviderTest {
    @RegisterExtension private static final HppcBytecodeExtension HPPC_ORDER_FIX = new HppcBytecodeExtension();

    private final CodecProvider provider = Testing.testStartup().getInstance(CodecProvider.class);

    @Test
    public void codecs_roundtrip() throws Exception {
        assertCodec(provider.getCodecOrDie(Integer.class)).roundtrip(0);
        assertCodec(provider.getCodecOrDie(Long.class)).roundtrip(0L);
        assertCodec(provider.getCodecOrDie(Int128.class)).roundtrip(Int128.ZERO);
        assertCodec(provider.getCodecOrDie(String.class)).roundtrip("");
        assertCodec(provider.getCodecOrDie(String.class)).roundtrip("foo");
        assertCodec(provider.getCodecOrDie(IntArrayList.class)).roundtrip(IntArrayList.from(1, 2, 3));
        assertCodec(provider.getCodecOrDie(IntHashSet.class)).roundtrip(IntHashSet.from(1, 2, 3));
        assertCodec(provider.getCodecOrDie(DefaultSession.class)).roundtrip(SessionBuilder.ofId(123).build());
        assertCodec(provider.getCodecOrDie(DefaultSession.class)).roundtrip(SessionBuilder.ofId(123).withoutIpAddress().build());
        assertCodec(provider.getCodecOrDie(DefaultUser.class)).roundtrip(UserBuilder.ofId(456).build());
        assertCodec(provider.getCodecOrDie(DefaultUser.class)).roundtrip(UserBuilder.ofAnyId(0).build());
    }
}
