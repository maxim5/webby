package io.spbx.util.process;

import com.google.common.flogger.FluentLogger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * https://stackoverflow.com/questions/808276/how-to-add-a-timeout-value-when-using-javas-runtime-exec
 */
public class ProcessRunner {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static int executeWithTimeout(@NotNull ProcessBuilder builder, long timeOut, @NotNull TimeUnit timeUnit) {
        try {
            log.at(Level.INFO).log("Running process: %s", builder.command());
            Process process = builder.start();
            long start = System.currentTimeMillis();

            new StreamGobbler(process.getErrorStream(), line -> {
                System.err.print("ERR > ");
                System.err.println(line);
            }).start();

            try {
                boolean isTimedOut = false;
                if (timeOut > 0) {
                    isTimedOut = process.waitFor(timeOut, timeUnit);
                } else {
                    process.waitFor();
                }
                if (isTimedOut) {
                    long runtime = System.currentTimeMillis() - start;
                    int exitValue = process.exitValue();
                    Level level = (exitValue == 0) ? Level.INFO : Level.SEVERE;
                    log.at(level).log("Process exit value: {}. Runtime: {} ms", exitValue, runtime);
                } else {
                    log.at(Level.SEVERE).log("The command [%s] timed out. Destroying process", builder);
                    process.destroy();
                }
            } catch (InterruptedException e) {
                log.at(Level.WARNING).withCause(e).log("The command [%s] interrupted: %s", builder, e.getMessage());
            } finally {
                if (process.isAlive()) {
                    process.destroy();
                }
            }

            return process.exitValue();
        } catch (IOException e) {
            log.at(Level.SEVERE).withCause(e).log("The command [%s] did not complete due to an IO error", builder.command());
        } catch (Throwable e) {
            log.at(Level.SEVERE).withCause(e).log("The command [%s] failed", builder.command());
        }

        return -1;
    }
}
