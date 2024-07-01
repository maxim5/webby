package io.spbx.util.collect;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ClassMapTest {
    @Test
    public void getClass_simple() {
        ClassMap<Object> classMap = ClassMap.mutable();
        classMap.putInstance("foo");
        classMap.putInstance(Integer.MAX_VALUE);
        classMap.putInstance(Long.MAX_VALUE);

        assertThat(classMap.getClass(String.class)).isEqualTo("foo");
        assertThat(classMap.getClass(Integer.class)).isEqualTo(Integer.MAX_VALUE);
        assertThat(classMap.getClass(Long.class)).isEqualTo(Long.MAX_VALUE);
        assertThat(classMap.getClass(Boolean.class)).isNull();
    }

    @Test
    public void getSuper_simple_inheritance() {
        ClassMap<Object> classMap = ClassMap.mutable();
        classMap.put(Object.class, "foo");
        assertThat(classMap.getSuper(String.class)).isEqualTo("foo");

        classMap.put(String.class, "bar");
        assertThat(classMap.getSuper(String.class)).isEqualTo("bar");
    }

    @Test
    public void getSuper_inheritance_three() {
        ClassMap<A> classMap = ClassMap.concurrent();

        classMap.put(A.class, a);
        assertThat(classMap.getSuper(A.class)).isSameInstanceAs(a);
        assertThat(classMap.getSuper(B.class)).isSameInstanceAs(a);
        assertThat(classMap.getSuper(C.class)).isSameInstanceAs(a);

        classMap.put(B.class, b);
        assertThat(classMap.getSuper(A.class)).isSameInstanceAs(a);
        assertThat(classMap.getSuper(B.class)).isSameInstanceAs(b);
        assertThat(classMap.getSuper(C.class)).isSameInstanceAs(b);

        classMap.put(C.class, c);
        assertThat(classMap.getSuper(A.class)).isSameInstanceAs(a);
        assertThat(classMap.getSuper(B.class)).isSameInstanceAs(b);
        assertThat(classMap.getSuper(C.class)).isSameInstanceAs(c);
    }

    private static class A {}
    private static class B extends A {}
    private static class C extends B {}

    private static final A a = new A();
    private static final B b = new B();
    private static final C c = new C();
}
