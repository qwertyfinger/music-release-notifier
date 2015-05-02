package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasestracker.events.NoReleasesEvent;
import com.qwertyfinger.musicreleasestracker.events.ReleasesFetchedEvent;
import com.qwertyfinger.musicreleasestracker.misc.Release;

import java.util.List;

import de.greenrobot.event.EventBus;

public class FetchReleasesJob extends Job{

    private final Context context;

    public FetchReleasesJob(Context context) {
        super(new Params(4).groupBy("database"));
        this.context = context;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (db.getArtistsCount() == 0) {
            EventBus.getDefault().post(new NoArtistsEvent());
        }

        if (db.getReleasesCount() == 0) {
            EventBus.getDefault().post(new NoReleasesEvent());
        }

        else {
            List<Release> releases = db.getAllReleases();
            EventBus.getDefault().post(new ReleasesFetchedEvent(releases));
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
