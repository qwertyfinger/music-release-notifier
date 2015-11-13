package com.qwertyfinger.musicreleasetracker.jobs.artist;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.events.artist.ArtistsFetchedEvent;
import com.qwertyfinger.musicreleasetracker.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasetracker.misc.Constants;

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
