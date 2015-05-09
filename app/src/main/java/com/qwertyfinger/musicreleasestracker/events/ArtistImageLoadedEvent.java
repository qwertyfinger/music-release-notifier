package com.qwertyfinger.musicreleasestracker.events;

import android.view.View;

public class ArtistImageLoadedEvent {
    private View view;

    public ArtistImageLoadedEvent(View view){
        this.view = view;
    }

    public View getView() {
        return view;
    }
}
