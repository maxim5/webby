package io.webby.db.model;

public interface SettableIntAutoIdModel extends IntAutoIdModel {
    void resetIdToAuto();

    void setIfAutoIdOrDie(int newId);
}
