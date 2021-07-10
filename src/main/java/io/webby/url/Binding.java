package io.webby.url;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public record Binding(String url, Method method, Annotation annotation) {
}
