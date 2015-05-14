package com.qwertyfinger.musicreleasetracker.events.artist;

import android.view.View;

import com.qwertyfinger.musicreleasetracker.entities.Artist;

public class ArtistAddedEvent {

    private Artist artist;
    private View view;

    public ArtistAddedEvent(Artist artist, View view){
        this.artist = artist;
        this.view = view;

    }

    public Artist getArtist() {
        return artist;
    }

    public View getView() {
        return view;
    }
}
