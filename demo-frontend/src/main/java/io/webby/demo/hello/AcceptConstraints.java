package io.webby.demo.hello;

import io.spbx.util.base.CharArray;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Param;
import io.webby.url.convert.CharArrayConstraint;
import io.webby.url.convert.Constraint;
import io.webby.url.convert.IntConverter;
import io.webby.url.convert.StringConstraint;

public class AcceptConstraints {
    @Param private static final Constraint<String> str = StringConstraint.MAX_256;
    @Param final Constraint<CharArray> array = new CharArrayConstraint(10);
    @Param static IntConverter val = new IntConverter(2, 99);
    @Param private static final Constraint<String> s = value -> String.valueOf(value).toLowerCase();

    @GET(url = "/constraints/one_string/{str}")
    public String one_string(String str) {
        return str.toUpperCase();
    }

    @GET(url = "/constraints/one_array/{array}")
    public String one_array(CharArray array) {
        return array.toString().toUpperCase();
    }

    @GET(url = "/constraints/one_int/{val}")
    public String one_int(int val) {
        return "%d".formatted(val);
    }

    @GET(url = "/constraints/convert/one_string/{s}")
    public String convert_one_string(String s) {
        return s;
    }
}
