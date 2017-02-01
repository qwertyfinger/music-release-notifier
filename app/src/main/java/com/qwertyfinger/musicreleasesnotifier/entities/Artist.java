package com.qwertyfinger.musicreleasesnotifier.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable {
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

    protected Artist(Parcel in) {
        title = in.readString();
        id = in.readString();
        image = in.readString();
    }

    @Override
    public boolean equals(Object object){
        if (!(object instanceof Artist)) return false;
        if (object == null) return false;
        if (object == this) return true;
        Artist other = (Artist) object;

        if (id.equals(other.id) && title.equals(other.title) && image.equals(other.image))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(id);
        dest.writeString(image);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}
