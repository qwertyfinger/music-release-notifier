package com.qwertyfinger.musicreleasestracker.events;

import com.qwertyfinger.musicreleasestracker.misc.Artist;

public class ArtistAddedEvent {

    private Artist artist;

    public ArtistAddedEvent(Artist artist){
        this.artist = artist;
    }

    public Artist getArtist() {
        return artist;
    }
}
