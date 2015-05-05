package com.qwertyfinger.musicreleasestracker.misc;

public class SearchResult {
    private String title;
    private String imageUrl;
    private String id;

    public SearchResult(String title, String imageUrl, String id){
        this.title = title;
        this.imageUrl = imageUrl;
        this.id = id;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}
