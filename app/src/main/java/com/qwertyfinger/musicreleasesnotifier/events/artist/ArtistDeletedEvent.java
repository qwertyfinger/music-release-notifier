package com.qwertyfinger.musicreleasesnotifier.events.artist;

import android.view.View;

import com.qwertyfinger.musicreleasesnotifier.entities.Artist;

public class ArtistDeletedEvent {

    private View view;
    private Artist artist;

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
