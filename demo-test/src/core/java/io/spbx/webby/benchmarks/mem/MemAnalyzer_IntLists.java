package io.spbx.webby.benchmarks.mem;

import com.carrotsearch.hppc.BoundedProportionalArraySizingStrategy;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.volkhart.memory.MemoryMeasurer;
import com.volkhart.memory.ObjectGraphMeasurer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Run with:
// -ea --illegal-access=permit -javaagent:demo-test/src/core/resources/jars/measurer-0.1.1.jar
public class MemAnalyzer_IntLists {
    private static void printObj(Object object) {
        System.out.println(object.getClass());
        System.out.println("objects: " + ObjectGraphMeasurer.measure(object));
        System.out.println("memory:  " + format(MemoryMeasurer.measureBytes(object)));
        System.out.println();
    }

    private static String format(long number) {
        return NumberFormat.getInstance(Locale.US).format(number);
    }

    public static void main(String[] args) {
        printObj(List.of(1, 2, 3));
        printObj(new ArrayList<>(List.of(1, 2, 3)));
        printObj(IntArrayList.from(1, 2, 3));
        printObj(IntArrayList.from(1, 2, 3).buffer);
        printObj(IntHashSet.from(1, 2, 3));
        printObj(BoundedProportionalArraySizingStrategy.DEFAULT_INSTANCE);

        // int[] array = new int[10000];
        // System.out.println(IntHashSet.from(array).ramBytesAllocated());
        // System.out.println(IntArrayList.from(array).ramBytesAllocated());
    }
}
