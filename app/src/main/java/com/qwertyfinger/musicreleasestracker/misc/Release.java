package com.qwertyfinger.musicreleasestracker.misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Release implements Comparable<Release> {
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

    @Override
    public int compareTo(Release other) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
        Date date1 = Calendar.getInstance().getTime();
        Date date2 = date1;
        try {
            date1 = formatter.parse(other.getDate());
            date2 = formatter.parse(releaseDate);
        } catch (ParseException e) {
        }
        if (date2.after(date1)) return 1;
        if (date2.before(date1)) return -1;
        if (title.subSequence(0, 1).charAt(0) > other.getTitle().subSequence(0, 1).charAt(0)) return 1;
        if (title.subSequence(0, 1).charAt(0) < other.getTitle().subSequence(0, 1).charAt(0)) return -1;
        if (artist.subSequence(0, 1).charAt(0) > other.getArtist().subSequence(0, 1).charAt(0)) return 1;
        if (artist.subSequence(0, 1).charAt(0) < other.getArtist().subSequence(0, 1).charAt(0)) return -1;
        return 0;
    }
}
