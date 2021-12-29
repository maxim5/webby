package io.webby.orm.api.query;

public interface LimitClause extends Filter, Representable {
    int limitValue();
}
