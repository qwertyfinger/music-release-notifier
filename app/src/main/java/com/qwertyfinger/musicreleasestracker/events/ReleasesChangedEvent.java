package com.qwertyfinger.musicreleasestracker.events;

public class ReleasesChangedEvent {

    private int actionId;

    public ReleasesChangedEvent(int actionId){
        this.actionId = actionId;
    }

    public int getActionId() {
        return actionId;
    }
}
