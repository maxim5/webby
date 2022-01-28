package io.webby.perf;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.volkhart.memory.MemoryMeasurer;
import com.volkhart.memory.ObjectGraphMeasurer;
import org.jctools.maps.NonBlockingHashMapLong;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;

// Run with:
// -ea --illegal-access=permit -javaagent:demo-test/src/core/resources/jars/measurer-0.1.1.jar
//
// See https://github.com/MariusVolkhart/memory-measurer
//     https://stackoverflow.com/questions/9368764/calculate-size-of-object-in-java
// Jar from https://mvnrepository.com/artifact/com.volkhart.memory/measurer
public class MemAnalyzer_Maps {
    private final IntObjectHashMap<IntHashSet> intObjectMap = new IntObjectHashMap<>();
    private final NonBlockingHashMapLong<IntHashSet> nonBlocking = new NonBlockingHashMapLong<>();
    private final ConcurrentHashMap<Integer, HashSet<Integer>> concurrentMap = new ConcurrentHashMap<>();
    private final ArrayListMultimap<Integer, Integer> multimap = ArrayListMultimap.create();

    public void ensureKey(int key) {
        intObjectMap.putIfAbsent(key, new IntHashSet());
        nonBlocking.putIfAbsent(key, new IntHashSet());
        concurrentMap.putIfAbsent(key, new HashSet<>());
    }

    public void add(int key, int value) {
        intObjectMap.get(key).add(value);
        nonBlocking.get(key).add(value);
        concurrentMap.get(key).add(value);
        multimap.put(key, value);
    }

    public void measure(String description) {
        System.out.println();
        System.out.println("******* " + description + " *******");
        System.out.println();

        printObj(intObjectMap,
                 IntObjectMap::size,
                 map -> Streams.stream(map).mapToInt(cursor -> cursor.value.size()).sum());

        printObj(nonBlocking,
                 Map::size,
                 map -> map.values().stream().mapToInt(IntHashSet::size).sum());

        printObj(concurrentMap,
                 Map::size,
                 map -> map.values().stream().mapToInt(Collection::size).sum());

        printObj(multimap,
                 Multimap::size,
                 map -> map.asMap().values().stream().mapToInt(Collection::size).sum());
    }

    private static <T> void printObj(T object, ToIntFunction<T> size, ToIntFunction<T> valuesSize) {
        System.out.println(object.getClass());
        System.out.println("size:      keys=" + size.applyAsInt(object) + " values=" + format(valuesSize.applyAsInt(object)));
        System.out.println("objects: " + ObjectGraphMeasurer.measure(object));
        System.out.println("memory:  " + format(MemoryMeasurer.measureBytes(object)));
        System.out.println();
    }

    private static String format(long number) {
        return NumberFormat.getInstance(Locale.US).format(number);
    }

    private static final int keys = 1000;
    private static final int values = 100;

    public static void main(String[] args) {
        MemAnalyzer_Maps check = new MemAnalyzer_Maps();
        check.measure("empty");

        for (int i = 0; i < keys; i++) {
            check.ensureKey(i);
        }
        check.measure(keys + " keys");

        for (int i = 0; i < keys; i++) {
            for (int j = 0; j < values; j++) {
                check.add(i, j + 1000);
            }
        }
        check.measure(values + " values per key");
    }
}
