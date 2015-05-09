package com.qwertyfinger.musicreleasestracker.events;

import com.qwertyfinger.musicreleasestracker.misc.Artist;

import java.util.List;

public class SearchQueryEvent {
    private List<Artist> searchResults;

    public SearchQueryEvent(List<Artist> searchResults){
        this.searchResults = searchResults;
    }

    public List<Artist> getSearchResults() {
        return searchResults;
    }
}
