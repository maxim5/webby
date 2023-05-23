package io.webby.netty.response;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.webby.netty.HttpConst;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static com.google.common.truth.Truth.assertThat;

public class HttpCachingTest {
    @ParameterizedTest
    @ValueSource(strings = {"Fri, 31 Dec 2021 13:03:15 CET", "Fri, 09 Apr 2021 23:55:38 GMT", "Wed May 19 11:21:05 GMT 2021"})
    public void isModifiedSince(String headerValue) {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set(HttpConst.IF_MODIFIED_SINCE, headerValue);

        assertThat(HttpCaching.isModifiedSince(toMillis("2015-12-03T10:15:30.00Z"), headers)).isFalse();
        assertThat(HttpCaching.isModifiedSince(toMillis("2020-12-03T10:15:30.00Z"), headers)).isFalse();
        assertThat(HttpCaching.isModifiedSince(toMillis("2022-12-03T10:15:30.00Z"), headers)).isTrue();
        assertThat(HttpCaching.isModifiedSince(toMillis("2035-12-03T10:15:30.00Z"), headers)).isTrue();
    }

    @Test
    public void lastModifiedValue() {
        assertThat(HttpCaching.lastModifiedValue(1618012538000L)).isEqualTo("Fri, 09 Apr 2021 23:55:38 GMT");
        assertThat(HttpCaching.lastModifiedValue(1621423265000L)).isEqualTo("Wed, 19 May 2021 11:21:05 GMT");
        assertThat(HttpCaching.lastModifiedValue(1640955795000L)).isEqualTo("Fri, 31 Dec 2021 13:03:15 GMT");
        assertThat(HttpCaching.lastModifiedValue(1641030397661L)).isEqualTo("Sat, 01 Jan 2022 09:46:37 GMT");
    }

    private static long toMillis(String date) {
        return Instant.parse(date).toEpochMilli();
    }
}
