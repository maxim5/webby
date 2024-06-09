package io.webby.testing.ext;

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
 */
// https://stackoverflow.com/questions/42804253/how-to-apply-remove-and-re-apply-the-bytebuddy-transformation
// https://stackoverflow.com/questions/71816195/redefine-methods-with-bytebuddy
// https://stackoverflow.com/questions/61148740/bytebuddy-agent-to-replace-one-method-param-with-another
public class HppcInstrumentationExtension implements BeforeEachCallback, AfterEachCallback {
    private ResettableClassFileTransformer resetter;

    public HppcInstrumentationExtension() {
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
