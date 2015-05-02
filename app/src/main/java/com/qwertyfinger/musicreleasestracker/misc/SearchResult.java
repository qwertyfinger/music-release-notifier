package com.qwertyfinger.musicreleasestracker.misc;

public class SearchResult {
    private String mName;
    private String mImageUrl;
    private String mbid;

    public SearchResult(String name, String imageUrl, String mbid){
        this.mName = name;
        this.mImageUrl = imageUrl;
        this.mbid = mbid;
    }


    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }
}
