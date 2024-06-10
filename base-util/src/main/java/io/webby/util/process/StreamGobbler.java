package io.webby.util.process;

import com.google.common.flogger.FluentLogger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Utility thread class which consumes and displays stream input.
 * <p>
 * Original code taken from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 */
public class StreamGobbler extends Thread {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final InputStream inputStream;
    private final Consumer<String> stream;

    public StreamGobbler(@NotNull InputStream inputStream, @NotNull Consumer<String> stream) {
        super("StreamGobbler");
        this.inputStream = inputStream;
        this.stream = stream;
    }

    /**
     * Consumes the output from the input stream and displays the lines consumed if configured to do so.
     */
    @Override
    public void run() {
        try {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = input.readLine()) != null) {
                    stream.accept(line);
                }
            }
        } catch (Throwable e) {
            log.at(Level.SEVERE).withCause(e).log("Failed to consume the input stream");
        }
    }
}
