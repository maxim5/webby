package io.webby.util.io;

import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.Set;

public class EasyNet {
    public static int nextAvailablePort() throws UncheckedIOException {
        // https://stackoverflow.com/questions/2675362/how-to-find-an-available-port
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(false);
            return socket.getLocalPort();
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        }
    }

    private static final Set<String> KNOWN_LOCAL_HOSTS = Set.of(
        "localhost",
        "127.0.0.1",
        "fe80:0:0:0:0:0:0:1%1",
        "0:0:0:0:0:0:0:1",
        "::1"
    );

    public static boolean isLocalhost(@NotNull String host) {
        return isKnownLocalhost(host) || isVerifiedLocalhost(host);
    }

    public static boolean isKnownLocalhost(@NotNull String host) {
        return KNOWN_LOCAL_HOSTS.contains(host.toLowerCase());
    }

    public static boolean isVerifiedLocalhost(@NotNull String host) {
        // https://stackoverflow.com/questions/2406341/how-to-check-if-an-ip-address-is-the-local-host-on-a-multi-homed-system
        try {
            return isLocalAddress(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static boolean isLocalAddress(@NotNull InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            return true;  // Local sub-net.
        }
        // Check if the non-local address is defined on any local-interface.
        try {
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (SocketException e) {
            return false;
        }
    }
}
