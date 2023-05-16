package io.webby.orm.api;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ForeignLongTest {
    @Test
    public void isMatch_simple() {
        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofId(1))).isTrue();
        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofEntity(1, "foo"))).isTrue();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofId(1))).isTrue();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofEntity(1, "foo"))).isTrue();

        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofId(2))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofId(1), ForeignLong.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofId(2))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignLong.isMatch(ForeignLong.ofEntity(1, "foo"), ForeignLong.ofEntity(1, "bar"))).isFalse();
    }
}
