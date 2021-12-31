package io.webby.demo.model;

import java.util.Objects;

public class InheritedModel extends InheritedModelBase {
    private int inheritedModelId;
    private boolean boolValue;

    private static int unrelated;
    private static final int CONST = 0;

    public InheritedModel(String str, int intValue, int inheritedModelId, boolean boolValue) {
        super(str, intValue);
        this.inheritedModelId = inheritedModelId;
        this.boolValue = boolValue;
    }

    public int getInheritedModelId() {
        return inheritedModelId;
    }

    public boolean isBoolValue() {
        return boolValue;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof InheritedModel that && super.equals(o) &&
               inheritedModelId == that.inheritedModelId && boolValue == that.boolValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inheritedModelId, boolValue);
    }
}

class InheritedModelBase {
    private String str;
    private int intValue;

    private static int unrelated;
    private static final int CONST = 0;

    public InheritedModelBase(String str, int intValue) {
        this.str = str;
        this.intValue = intValue;
    }

    public String getStr() {
        return str;
    }

    public int getIntValue() {
        return intValue;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof InheritedModelBase that && intValue == that.intValue && str.equals(that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(str, intValue);
    }
}
