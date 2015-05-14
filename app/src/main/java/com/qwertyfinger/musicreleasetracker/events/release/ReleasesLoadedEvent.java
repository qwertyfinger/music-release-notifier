package com.qwertyfinger.musicreleasetracker.events.release;

import com.qwertyfinger.musicreleasetracker.entities.Release;

import java.util.List;

public class ReleasesLoadedEvent {

    private List<Release> releases;

    public ReleasesLoadedEvent(List<Release> releases) {
        this.releases = releases;
    }

    public List<Release> getReleases() {
        return releases;
    }
}
