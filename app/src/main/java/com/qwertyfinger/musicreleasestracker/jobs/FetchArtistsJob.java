package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ArtistsFetchedEvent;
import com.qwertyfinger.musicreleasestracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasestracker.misc.Artist;

import java.util.List;

import de.greenrobot.event.EventBus;

public class FetchArtistsJob extends Job {

    private final Context context;

    public FetchArtistsJob(Context context) {
        super(new Params(Constants.JOB_PRIORITY_MEDIUM).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (db.getArtistsCount() == 0)
            EventBus.getDefault().post(new NoArtistsEvent());

        else {
            List<Artist> artists = db.getAllArtists();
            EventBus.getDefault().post(new ArtistsFetchedEvent(artists));
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
