package io.webby.orm.api;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ForeignObjTest {
    @Test
    public void isMatch_simple() {
        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofId(1))).isTrue();
        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofEntity(1, "foo"))).isTrue();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofId(1))).isTrue();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofEntity(1, "foo"))).isTrue();

        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofId(2))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofId(1), ForeignObj.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofId(2))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofEntity(2, "bar"))).isFalse();
        assertThat(ForeignObj.isMatch(ForeignObj.ofEntity(1, "foo"), ForeignObj.ofEntity(1, "bar"))).isFalse();
    }
}
