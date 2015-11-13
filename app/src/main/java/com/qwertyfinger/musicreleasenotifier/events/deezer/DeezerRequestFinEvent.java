package com.qwertyfinger.musicreleasetracker.events.deezer;


import com.deezer.sdk.model.Artist;

import java.util.List;

public class DeezerRequestFinEvent {

    private List<Artist> artists;

    public DeezerRequestFinEvent(List<Artist> artists) {
        this.artists = artists;
    }

    public List<Artist> getArtists() {
        return artists;
    }
}
