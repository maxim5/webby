package io.webby.orm.api.query;

public interface SelectQuery extends Representable, ArgsHolder {
    int columnsNumber();
}
