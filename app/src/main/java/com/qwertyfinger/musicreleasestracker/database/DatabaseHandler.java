package com.qwertyfinger.musicreleasestracker.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.qwertyfinger.musicreleasestracker.events.ArtistExistsEvent;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.qwertyfinger.musicreleasestracker.misc.Release;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DatabaseHandler  extends SQLiteOpenHelper{

    private static DatabaseHandler sInstance;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mainDatabase";

    //create releases table
    private static final String CREATE_TABLE_RELEASES = "CREATE TABLE "
            + ReleasesContract.ReleasesTable.TABLE_NAME + "(" + ReleasesContract.ReleasesTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," + ReleasesContract.ReleasesTable.COLUMN_NAME_TITLE
            + " TEXT DEFAULT 'TBA'," + ReleasesContract.ReleasesTable.COLUMN_NAME_ARTIST + " TEXT NOT NULL," + ReleasesContract.ReleasesTable.COLUMN_NAME_DATE + " TEXT DEFAULT 'TBA',"
            + ReleasesContract.ReleasesTable.COLUMN_NAME_IMAGE + " TEXT," + ReleasesContract.ReleasesTable.COLUMN_NAME_ARTISTID + " TEXT NOT NULL" + ")";

    // create artists table
    private static final String CREATE_TABLE_ARTSITS = "CREATE TABLE "
            + ArtistsContract.ArtistsTable.TABLE_NAME + "(" + ArtistsContract.ArtistsTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," + ArtistsContract.ArtistsTable.COLUMN_NAME_TITLE
            + " TEXT NOT NULL," + ArtistsContract.ArtistsTable.COLUMN_NAME_IMAGE + " TEXT" + ")";


    public static synchronized DatabaseHandler getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RELEASES);
        db.execSQL(CREATE_TABLE_ARTSITS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ArtistsContract.ArtistsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReleasesContract.ReleasesTable.TABLE_NAME);

        onCreate(db);
    }

//    <--- RELEASE CRUD --->

    public void addReleases(List<Release> releases){

        if (releases.size() > 0) {

            SQLiteDatabase db = this.getWritableDatabase();

            String sql = "INSERT OR REPLACE INTO " + ReleasesContract.ReleasesTable.TABLE_NAME + " VALUES (COALESCE((SELECT " + ReleasesContract.ReleasesTable.COLUMN_NAME_ID + " FROM "
                    + ReleasesContract.ReleasesTable.TABLE_NAME + " WHERE " + ReleasesContract.ReleasesTable.COLUMN_NAME_ID + " = ?), ?), "
                    + "COALESCE(?, 'TBA'), " +
                    "COALESCE((SELECT " + ReleasesContract.ReleasesTable.COLUMN_NAME_ARTIST + " FROM " + ReleasesContract.ReleasesTable.TABLE_NAME
                    + " WHERE " + ReleasesContract.ReleasesTable.COLUMN_NAME_ID + " = ?), ?), "
                    + "COALESCE(?, 'TBA'), COALESCE((SELECT " + ReleasesContract.ReleasesTable.COLUMN_NAME_IMAGE + " FROM "
                    + ReleasesContract.ReleasesTable.TABLE_NAME + " WHERE " + ReleasesContract.ReleasesTable.COLUMN_NAME_ID + " = ?), ?), " +
                    "COALESCE((SELECT " + ReleasesContract.ReleasesTable.COLUMN_NAME_ARTISTID + " FROM " + ReleasesContract.ReleasesTable.TABLE_NAME
                    + " WHERE " + ReleasesContract.ReleasesTable.COLUMN_NAME_ID + " = ?), ?));";
            SQLiteStatement statement = db.compileStatement(sql);

            db.beginTransaction();

            try {
                for (Release release : releases) {
                    statement.bindString(1, release.getId());
                    statement.bindString(2, release.getId());
                    statement.bindString(3, release.getTitle());
                    statement.bindString(4, release.getId());
                    statement.bindString(5, release.getArtist());
                    statement.bindString(6, release.getDate());
                    statement.bindString(7, release.getId());
                    statement.bindString(8, release.getImage());
                    statement.bindString(9, release.getId());
                    statement.bindString(10, release.getArtistId());
                    statement.execute();
                    statement.clearBindings();
                }

                db.setTransactionSuccessful();

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                db.endTransaction();
            }
        }
    }

    public Release getRelease(String id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ReleasesContract.ReleasesTable.TABLE_NAME, new String[]{ReleasesContract.ReleasesTable.COLUMN_NAME_ID,
                        ReleasesContract.ReleasesTable.COLUMN_NAME_TITLE, ReleasesContract.ReleasesTable.COLUMN_NAME_ARTIST,
                        ReleasesContract.ReleasesTable.COLUMN_NAME_DATE, ReleasesContract.ReleasesTable.COLUMN_NAME_IMAGE},
                ReleasesContract.ReleasesTable.COLUMN_NAME_ID + "=?",
                new String[]{id}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Release release = new Release(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        return release;
    }

    public List<Release> getAllReleases() {
        List<Release> releaseList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + ReleasesContract.ReleasesTable.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Release release = new Release(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                releaseList.add(release);
            } while (cursor.moveToNext());
        }

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        Collections.sort(releaseList);

        return releaseList;
    }

    public boolean isThereReleaseByArtist(String artistId){
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT  * FROM " + ReleasesContract.ReleasesTable.TABLE_NAME + " WHERE " + ReleasesContract.ReleasesTable.COLUMN_NAME_ARTISTID
                + " = '" + artistId +"'";
        Cursor cursor = db.rawQuery(countQuery, null);
        if(cursor != null && !cursor.isClosed()){
            if (cursor.getCount() == 0) {
                cursor.close();
                return false;
            }
            cursor.close();
        }

        return true;
    }

    public void deleteReleasesByArtist(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ReleasesContract.ReleasesTable.TABLE_NAME, ReleasesContract.ReleasesTable.COLUMN_NAME_ARTISTID + " = ?", new String[]{id});
    }

    public void deleteAllReleases(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM " + ReleasesContract.ReleasesTable.TABLE_NAME);
    }

    public int getReleasesCount() {
        int count = 0;
        String countQuery = "SELECT  * FROM " + ReleasesContract.ReleasesTable.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        if(cursor != null && !cursor.isClosed()){
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }

    //    <--- ARTIST CRUD --->


    public void addArtist(Artist artist) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "INSERT INTO " + ArtistsContract.ArtistsTable.TABLE_NAME + " VALUES (?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);

        db.beginTransaction();

        try {
            statement.bindString(1, artist.getId());
            statement.bindString(2, artist.getTitle());
            statement.bindString(3, artist.getImage());
            statement.execute();
            statement.clearBindings();

            db.setTransactionSuccessful();
        } catch (SQLiteConstraintException e) {
            EventBus.getDefault().post(new ArtistExistsEvent());
        } finally {
            db.endTransaction();
        }
    }

    public boolean isArtistAdded(String id){
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT  * FROM " + ArtistsContract.ArtistsTable.TABLE_NAME + " WHERE " + ArtistsContract.ArtistsTable.COLUMN_NAME_ID
                + " = '" + id +"'";
        Cursor cursor = db.rawQuery(countQuery, null);
        if(cursor != null && !cursor.isClosed()){
            if (cursor.getCount() == 0) {
                cursor.close();
                return false;
            }
            cursor.close();
        }

        return true;
    }

    public Artist getArtist(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ArtistsContract.ArtistsTable.TABLE_NAME, new String[] {ArtistsContract.ArtistsTable.COLUMN_NAME_ID,
                        ArtistsContract.ArtistsTable.COLUMN_NAME_TITLE, ArtistsContract.ArtistsTable.COLUMN_NAME_IMAGE },
                ArtistsContract.ArtistsTable.COLUMN_NAME_ID + "=?",
                new String[] { id }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Artist artist = new Artist(cursor.getString(0), cursor.getString(1), cursor.getString(2));

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        return artist;
    }


    public List<Artist> getAllArtists() {
        List<Artist> artistList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + ArtistsContract.ArtistsTable.TABLE_NAME + " ORDER BY " + ArtistsContract.ArtistsTable.COLUMN_NAME_TITLE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Artist artist = new Artist(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                artistList.add(artist);
            } while (cursor.moveToNext());
        }

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        return artistList;
    }

    public void deleteArtist(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ArtistsContract.ArtistsTable.TABLE_NAME, ArtistsContract.ArtistsTable.COLUMN_NAME_ID + " = ?", new String[] { id });
    }

    public void deleteAllArtists(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM " + ArtistsContract.ArtistsTable.TABLE_NAME);
    }

    public int getArtistsCount() {
        int count = 0;
        String countQuery = "SELECT  * FROM " + ArtistsContract.ArtistsTable.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        if(cursor != null && !cursor.isClosed()){
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }
}
