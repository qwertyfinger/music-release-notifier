package com.qwertyfinger.musicreleasesnotifier.events.release;

import com.qwertyfinger.musicreleasesnotifier.entities.Release;

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
