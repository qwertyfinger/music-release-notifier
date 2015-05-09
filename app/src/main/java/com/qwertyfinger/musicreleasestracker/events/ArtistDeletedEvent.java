package com.qwertyfinger.musicreleasestracker.events;

import android.view.View;

import com.qwertyfinger.musicreleasestracker.misc.Artist;

public class ArtistDeletedEvent {

    private View view;
    private Artist artist;

    public ArtistDeletedEvent(){}

    public ArtistDeletedEvent(View view, Artist artist){
        this.view = view;
        this.artist = artist;
    }

    public View getView() {
        return view;
    }

    public Artist getArtist() {
        return artist;
    }
}
