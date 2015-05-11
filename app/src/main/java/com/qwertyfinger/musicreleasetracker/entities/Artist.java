package com.qwertyfinger.musicreleasetracker.entities;

public class Artist {
    private String title;
    private String id;
    private String image;


    public Artist(String id, String title, String image){
        this.id = id;
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }
}