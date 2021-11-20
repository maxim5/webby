package io.webby.db.model;

public interface IntAutoIdModel {
    int AUTO_ID = 0;

    boolean isAutoId();

    void resetIdToAuto();

    void setIfAutoIdOrDie(int newId);
}
