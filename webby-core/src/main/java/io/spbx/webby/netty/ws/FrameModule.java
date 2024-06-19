package io.spbx.webby.netty.ws;

import com.google.inject.AbstractModule;
import io.spbx.webby.netty.ws.sender.ChannelSender;
import io.spbx.webby.netty.ws.sender.Sender;

public class FrameModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Sender.class).to(ChannelSender.class);  // not a singleton!
        // MessageSender.class must use @ImplementedBy to handle generics (not ideal, but works).
        // bind(MessageSender.class).to(ChannelMessageSender.class);
    }
}
