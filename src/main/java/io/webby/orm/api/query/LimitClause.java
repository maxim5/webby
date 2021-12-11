package io.webby.orm.api.query;

public interface LimitClause extends Clause, Representable {
    int limitValue();
}
