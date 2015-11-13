package com.qwertyfinger.musicreleasetracker.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class Release implements Comparable<Release>, Parcelable {
    private String id;
    private String title;
    private String artist;
    private String releaseDate;
    private String image;
    private String artistId;

    public Release(String id, String title, String artist, String releaseDate, String image, String artistId){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.releaseDate = releaseDate;
        this.image = image;
        this.artistId = artistId;
    }

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
    public boolean equals(Object object){
        if (object == null) return false;
        if (object == this) return true;
        if (!(object instanceof Release)) return false;
        Release other = (Release) object;

        if (id.equals(other.id) && title.equals(other.title) && artist.equals(other.artist) && releaseDate.equals
                (other.releaseDate) && image.equals(other.image))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Release other) {
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
        Date date1 = Calendar.getInstance().getTime();
        Date date2 = date1;
        try {
            date1 = formatter.parse(other.getDate());
            date2 = formatter.parse(releaseDate);
        } catch (ParseException e) {}

        if (date2.after(date1)) return 1;
        if (date2.before(date1)) return -1;
        if (title.subSequence(0, 1).charAt(0) > other.getTitle().subSequence(0, 1).charAt(0)) return 1;
        if (title.subSequence(0, 1).charAt(0) < other.getTitle().subSequence(0, 1).charAt(0)) return -1;
        if (artist.subSequence(0, 1).charAt(0) > other.getArtist().subSequence(0, 1).charAt(0)) return 1;
        if (artist.subSequence(0, 1).charAt(0) < other.getArtist().subSequence(0, 1).charAt(0)) return -1;
        return 0;
    }

    public String getArtistId() {
        return artistId;
    }

    protected Release(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        releaseDate = in.readString();
        image = in.readString();
        artistId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(releaseDate);
        dest.writeString(image);
        dest.writeString(artistId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Release> CREATOR = new Parcelable.Creator<Release>() {
        @Override
        public Release createFromParcel(Parcel in) {
            return new Release(in);
        }

        @Override
        public Release[] newArray(int size) {
            return new Release[size];
        }
    };
}