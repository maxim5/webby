package io.webby.benchmarks.jmh;

import io.webby.util.base.FastFormat;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 3000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 5000, timeUnit = TimeUnit.MILLISECONDS)
public class FastFormatBenchmark {
    private static final int LOOPS = 1000;

    @Benchmark
    public int str_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("What do you get if you multiply %s?", counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str_new() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("What do you get if you multiply %s?", counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str_str_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("What do you get if you multiply %s and %s?", counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str_str_new() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("What do you get if you multiply %s and %s?", counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str_str_int_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("What do you get if you multiply %s and %s and %d?", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str_str_int_new() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("What do you get if you multiply %s and %s and %d?", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int short_only_specs_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("%s%d%s", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int short_only_specs_new() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("%s%d%s", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(FastFormatBenchmark.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(options).run();
    }
}
