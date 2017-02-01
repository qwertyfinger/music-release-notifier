package com.qwertyfinger.musicreleasesnotifier.events.artist;

import com.qwertyfinger.musicreleasesnotifier.entities.Artist;

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
