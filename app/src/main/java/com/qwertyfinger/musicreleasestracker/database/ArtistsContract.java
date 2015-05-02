package com.qwertyfinger.musicreleasestracker.database;

import android.provider.BaseColumns;

public class ArtistsContract {
    private ArtistsContract(){}

    public static abstract class ArtistsTable implements BaseColumns {
        public static final String TABLE_NAME = "artists";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "name";
        public static final String COLUMN_NAME_IMAGE = "image";
    }
}
