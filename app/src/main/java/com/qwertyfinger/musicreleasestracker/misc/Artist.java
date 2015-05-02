package com.qwertyfinger.musicreleasestracker.misc;

public class Artist {
    private String title;
    private String id;
    private String imageUri;


    public Artist(String id, String title, String imageUri){
        this.id = id;
        this.title = title;
        this.imageUri = imageUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
