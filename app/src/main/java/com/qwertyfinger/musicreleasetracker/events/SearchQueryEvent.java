package com.qwertyfinger.musicreleasetracker.events;

import com.qwertyfinger.musicreleasetracker.entities.Artist;

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
