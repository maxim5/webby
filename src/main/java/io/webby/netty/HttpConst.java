package io.webby.netty;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.AsciiString;

public class HttpConst {
    public static final AsciiString CACHE_CONTROL = HttpHeaderNames.CACHE_CONTROL;
    public static final AsciiString ETAG = HttpHeaderNames.ETAG;
    public static final AsciiString IF_MODIFIED_SINCE = HttpHeaderNames.IF_MODIFIED_SINCE;
    public static final AsciiString IF_NONE_MATCH = HttpHeaderNames.IF_NONE_MATCH;
    public static final AsciiString LAST_MODIFIED = HttpHeaderNames.LAST_MODIFIED;

    public static final AsciiString CONTENT_DISPOSITION = HttpHeaderNames.CONTENT_DISPOSITION;
    public static final AsciiString CONTENT_SECURITY_POLICY = HttpHeaderNames.CONTENT_SECURITY_POLICY;
    public static final AsciiString CONTENT_LENGTH = HttpHeaderNames.CONTENT_LENGTH;
    public static final AsciiString CONTENT_TYPE = HttpHeaderNames.CONTENT_TYPE;
    public static final AsciiString LOCATION = HttpHeaderNames.LOCATION;
    public static final AsciiString SERVER_TIMING = AsciiString.of("Server-Timing");
    public static final AsciiString SET_COOKIE = HttpHeaderNames.SET_COOKIE;
    public static final AsciiString X_FRAME_OPTIONS = HttpHeaderNames.X_FRAME_OPTIONS;
    public static final AsciiString X_XSS_PROTECTION = AsciiString.of("X-XSS-Protection");
    public static final AsciiString X_CONTENT_TYPE_OPTIONS = AsciiString.of("X-Content-Type-Options");

    public static final AsciiString APPLICATION_JSON = HttpHeaderValues.APPLICATION_JSON;
    public static final AsciiString TEXT_HTML = HttpHeaderValues.TEXT_HTML;
    public static final AsciiString TEXT_HTML_UTF8 = AsciiString.cached("text/html; charset=UTF-8");
    public static final AsciiString TEXT_PLAIN = HttpHeaderValues.TEXT_PLAIN;
    public static final AsciiString TEXT_PLAIN_UTF8 = AsciiString.cached("text/plain; charset=UTF-8");

    public static final AsciiString INLINE = AsciiString.of("inline");
    public static final AsciiString SAMEORIGIN = AsciiString.of("SAMEORIGIN");
    public static final AsciiString MODE_BLOCK = AsciiString.of("1;mode=block");
    public static final AsciiString SCRIPT_SRC_SELF = AsciiString.of("script-src 'self'");
    public static final AsciiString NOSNIFF = AsciiString.of("nosniff");
}
