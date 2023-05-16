package io.webby.db.model;

public interface SettableLongAutoIdModel extends LongAutoIdModel {
    void resetIdToAuto();

    void setIfAutoIdOrDie(long newId);
}
