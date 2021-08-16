package io.webby.testing;

import io.webby.ws.ClientFrameType;
import io.webby.ws.ClientInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FakeClients {
    public static ClientInfo DEFAULT = new ClientInfo(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static @NotNull ClientInfo client(@Nullable String version, @Nullable ClientFrameType frameType) {
        return new ClientInfo(Optional.ofNullable(version), Optional.ofNullable(frameType), Optional.empty(), Optional.empty());
    }

    public static @NotNull ClientInfo client(@NotNull ClientFrameType frameType) {
        return client(null, frameType);
    }
}
