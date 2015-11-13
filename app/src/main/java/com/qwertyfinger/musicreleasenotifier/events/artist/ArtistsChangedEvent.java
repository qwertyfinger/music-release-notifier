package com.qwertyfinger.musicreleasetracker.events.artist;

import com.qwertyfinger.musicreleasetracker.entities.Artist;

import java.util.List;

public class ArtistsChangedEvent {
    private List<Artist> artists;

    public ArtistsChangedEvent (List<Artist> artists) {
        this.artists = artists;
    }

    public List<Artist> getArtists() {
        return artists;
    }
}
