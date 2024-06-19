package io.spbx.webby.demo.hello;

import io.spbx.util.base.CharArray;
import io.spbx.webby.url.annotate.GET;

public class AcceptStrings {
    @GET(url = "/strings/one_string/{str}")
    public String one_string(String str) {
        return str.toUpperCase();
    }

    @GET(url = "/strings/one_array/{array}")
    public String one_array(CharArray array) {
        return array.toString().toUpperCase();
    }

    @GET(url = "/strings/one_char_sequence/{seq}")
    public String one_char_sequence(CharSequence seq) {
        return seq.toString().toUpperCase();
    }

    @GET(url = "/strings/two_strings/{str1}/{str2}")
    public String two_strings(String str1, String str2) {
        return "%s-%s".formatted(str1, str2);
    }

    @GET(url = "/strings/two_arrays/{array1}/{array2}")
    public String two_arrays(CharArray array1, CharArray array2) {
        return "%s.%s".formatted(array1, array2);
    }

    @GET(url = "/strings/two_char_sequences/{seq1}/{seq2}")
    public String two_char_sequences(CharSequence seq1, CharSequence seq2) {
        return "%s*%s".formatted(seq1, seq2);
    }

    @GET(url = "/strings/two_string_and_array/{str}/{array}")
    public String two_string_and_array(String str, CharArray array) {
        return "%s:%s".formatted(str, array);
    }
}
