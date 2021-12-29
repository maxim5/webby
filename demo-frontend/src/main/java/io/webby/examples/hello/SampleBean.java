package io.webby.examples.hello;

import java.util.List;
import java.util.Objects;

public class SampleBean {
    private int x;
    private String s;
    private List<Integer> list;

    public SampleBean() {
    }

    public SampleBean(int x, String s, List<Integer> list) {
        this.x = x;
        this.s = s;
        this.list = list;
    }

    public int getX() {
        return x;
    }

    public String getS() {
        return s;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setS(String s) {
        this.s = s;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SampleBean sample && x == sample.x && Objects.equals(s, sample.s) && Objects.equals(list, sample.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, s, list);
    }

    @Override
    public String toString() {
        return "Sample{x=%d, s='%s', list=%s}".formatted(x, s, list);
    }
}
