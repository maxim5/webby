package io.spbx.webby.benchmarks.jmh;

import com.google.common.base.Strings;
import io.spbx.util.base.FastFormat;
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
public class FastFormatJmhBenchmark {
    private static final int LOOPS = 1000;

    @Benchmark
    public int str1_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("What do you get if you multiply %s?", counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str1_gua() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = Strings.lenientFormat("What do you get if you multiply %s?", counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str1_new_i() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("What do you get if you multiply %s?", counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str1_new_o() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("What do you get if you multiply %s?", (Object) counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str2_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("What do you get if you multiply %s and %s?", counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str2_gua() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = Strings.lenientFormat("What do you get if you multiply %s and %s?", counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str2_new() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("What do you get if you multiply %s and %s?", counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str3_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("What do you get if you multiply %s and %s and %s?", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str3_gua() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = Strings.lenientFormat("What do you get if you multiply %s and %s and %s?", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int str3_new() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("What do you get if you multiply %s and %s and %s?", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int only_specs_std() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = String.format("%s%s%s", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int only_specs_gua() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = Strings.lenientFormat("%s%s%s", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    @Benchmark
    public int only_specs_new() {
        int counter = 0;
        for (int i = 0; i < LOOPS; i++) {
            String s = FastFormat.format("%s%s%s", counter, counter, counter);
            counter += s.length();
        }
        return counter;
    }

    public static void main(String[] args) throws RunnerException {
        System.setProperty("jmh.separateClasspathJAR", "true");
        Options options = new OptionsBuilder().include(FastFormatJmhBenchmark.class.getSimpleName()).build();
        new Runner(options).run();
    }
}
