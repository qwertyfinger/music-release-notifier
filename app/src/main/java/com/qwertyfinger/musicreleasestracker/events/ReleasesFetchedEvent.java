package com.qwertyfinger.musicreleasestracker.events;

import com.qwertyfinger.musicreleasestracker.misc.Release;

import java.util.List;

public class ReleasesFetchedEvent {

    private List<Release> releases;

    public ReleasesFetchedEvent(List<Release> releases){
        this.releases = releases;
    }

    public List<Release> getReleases() {
        return releases;
    }
}
