package io.webby.orm.api.query;

public interface SelectQuery extends Representable, HasArgs {
    int columnsNumber();
}
