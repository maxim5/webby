package io.spbx.util.base;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Fork(value = 1, warmups = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
public class Int128JmhBenchmark {
    private static final int N = 2000;
    private static final Random RAND = new Random(42);
    private static final long[] LONGS = IntStream.range(0, N).mapToLong(i -> RAND.nextLong()).toArray();
    private static final List<Int128> DOUBLES_64 = Arrays.stream(LONGS).mapToObj(Int128::from).toList();
    private static final List<Int128> DOUBLES_128 = Arrays.stream(LONGS).mapToObj(v -> Int128.fromBits(v, ~v)).toList();
    private static final List<BigInteger> BIGS_64 = DOUBLES_64.stream().map(Int128::toBigInteger).toList();
    private static final List<BigInteger> BIGS_128 = DOUBLES_128.stream().map(Int128::toBigInteger).toList();

    @Benchmark
    public void Int128_divide_64(Blackhole blackhole) {
        for (Int128 x : DOUBLES_128) {
            for (Int128 y : DOUBLES_64) {
                Int128 z = x.divide(y);
                blackhole.consume(z);
            }
        }
    }

    @Benchmark
    public void Int128_divide_128(Blackhole blackhole) {
        for (Int128 x : DOUBLES_128) {
            for (Int128 y : DOUBLES_128) {
                Int128 z = x.divide(y);
                blackhole.consume(z);
            }
        }
    }

    @Benchmark
    public void BigInteger_divide_64(Blackhole blackhole) {
        for (BigInteger x : BIGS_128) {
            for (BigInteger y : BIGS_64) {
                BigInteger z = x.divide(y);
                blackhole.consume(z);
            }
        }
    }

    @Benchmark
    public void BigInteger_divide_128(Blackhole blackhole) {
        for (BigInteger x : BIGS_128) {
            for (BigInteger y : BIGS_128) {
                BigInteger z = x.divide(y);
                blackhole.consume(z);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println(Arrays.toString(LONGS));
        System.setProperty("jmh.separateClasspathJAR", "true");
        Options options = new OptionsBuilder().include(Int128JmhBenchmark.class.getSimpleName()).build();
        new Runner(options).run();
    }
}