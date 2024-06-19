package io.spbx.util.testing.ext;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.LongHashSet;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.implementation.FixedValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static net.bytebuddy.matcher.ElementMatchers.anyOf;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Addresses <a href="https://github.com/carrotsearch/hppc/issues/14">Random iteration</a> HPPC feature in tests.
 * <p>
 * This extension applies byte-code manipulation to intercept calls to
 * {@link IntHashSet#nextIterationSeed()},
 * {@link LongHashSet#nextIterationSeed()},
 * etc.
 * Interception adds performance penalty per each call, which makes the test slightly slower.
 * But it fixes the order for <b>all</b> iterations for all instances within the test.
 *
 * @link <a href="https://stackoverflow.com/questions/42804253/how-to-apply-remove-and-re-apply-the-bytebuddy-transformation">(1)</a>
 * @link <a href="https://stackoverflow.com/questions/71816195/redefine-methods-with-bytebuddy">(2)</a>
 * @link <a href="https://stackoverflow.com/questions/61148740/bytebuddy-agent-to-replace-one-method-param-with-another">(3)</a>
 */
@SuppressWarnings("JavadocReference")
public class HppcBytecodeExtension implements BeforeEachCallback, AfterEachCallback {
    private ResettableClassFileTransformer resetter;

    public HppcBytecodeExtension() {
        ByteBuddyAgent.install();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        resetter = installAgent();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (resetter != null) {
            resetter.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        }
    }

    private @NotNull ResettableClassFileTransformer installAgent() {
        return new AgentBuilder
            .Default()
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .disableClassFormatChanges()
            .type(anyOf(IntHashSet.class, LongHashSet.class, IntIntHashMap.class, IntObjectHashMap.class))
            .transform(
                (typeBuilder, typeDescription, classLoader, module, protectionDomain) ->
                    typeBuilder
                        .method(named("nextIterationSeed"))
                        .intercept(FixedValue.value(0))
            ).installOnByteBuddyAgent();
    }
}
