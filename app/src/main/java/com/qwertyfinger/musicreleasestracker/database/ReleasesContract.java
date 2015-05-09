package com.qwertyfinger.musicreleasestracker.database;

import android.provider.BaseColumns;

public class ReleasesContract {
    private ReleasesContract(){}

    public static abstract class ReleasesTable implements BaseColumns {
        public static final String TABLE_NAME = "releases";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_ARTISTID = "artistId";
    }
}
