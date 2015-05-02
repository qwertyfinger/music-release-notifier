package com.qwertyfinger.musicreleasestracker.events;

import com.qwertyfinger.musicreleasestracker.misc.SearchResult;

import java.util.List;

public class SearchQueryEvent {
    private List<SearchResult> searchResults;

    public SearchQueryEvent(List<SearchResult> searchResults){
        this.searchResults = searchResults;
    }

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }
}
