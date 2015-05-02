package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.App;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasestracker.events.NoArtistsEvent;

import de.greenrobot.event.EventBus;

public class EmptyArtistsJob extends Job {

    private final Context context;
    private JobManager jobManager;

    public EmptyArtistsJob(Context context) {
        super(new Params(1).groupBy("database"));
        this.context = context;
        jobManager = App.getInstance().getJobManager();
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
        else {
            db.deleteAllArtists();
            EventBus.getDefault().post(new ArtistDeletedEvent());
            jobManager.addJobInBackground(new EmptyReleasesJob(context));
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
