package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ArtistExistsEvent;
import com.qwertyfinger.musicreleasestracker.events.ArtistAddedEvent;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.qwertyfinger.musicreleasestracker.misc.SearchResult;

import de.greenrobot.event.EventBus;

public class AddArtistJob extends Job{

    private final Context context;
    private final SearchResult searchResult;

    public AddArtistJob(Context c, SearchResult searchResult) {
        super(new Params(2).groupBy("database"));
        this.context = c;
        this.searchResult = searchResult;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        try {
            db.addArtist(new Artist(searchResult.getMbid(), searchResult.getName(), searchResult. getImageUrl()));
            EventBus.getDefault().post(new ArtistAddedEvent());
        } catch (SQLiteConstraintException e) {
            EventBus.getDefault().post(new ArtistExistsEvent());
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
