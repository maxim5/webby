package io.webby.db.model;

public interface LongAutoIdModel {
    long AUTO_ID = 0;

    boolean isAutoId();

    void resetIdToAuto();

    void setIfAutoIdOrDie(long newId);
}
