package com.qwertyfinger.musicreleasetracker.events;

import com.qwertyfinger.musicreleasetracker.entities.Artist;

import java.util.List;

public class ArtistsFetchedEvent {
    private List<Artist> artists;

    public ArtistsFetchedEvent(List<Artist> artists){
        this.artists = artists;
    }

    public List<Artist> getArtists() {
        return artists;
    }
}
