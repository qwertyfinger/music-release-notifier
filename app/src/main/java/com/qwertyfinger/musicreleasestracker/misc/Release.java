package com.qwertyfinger.musicreleasestracker.misc;

public class Release {
    private String id;
    private String title;
    private String artist;
    private String releaseDate;
    private String image;


    public Release(String id, String title, String artist, String releaseDate, String image){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.releaseDate = releaseDate;
        this.image = image;
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDate() {
        return releaseDate;
    }

    public String getImage() {
        return image;
    }
}
