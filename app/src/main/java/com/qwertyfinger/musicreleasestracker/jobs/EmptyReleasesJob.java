package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasestracker.misc.Release;

import java.util.List;

import de.greenrobot.event.EventBus;

public class EmptyReleasesJob extends Job{
    private final Context context;

    public EmptyReleasesJob(Context context) {
        super(new Params(Constants.JOB_PRIORITY_HIGH).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
    }


    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (db.getReleasesCount() > 0) {
            List<Release> releases = db.getAllReleases();
            for (Release release: releases){
                context.deleteFile(release.getImage());
            }

            db.deleteAllReleases();
            EventBus.getDefault().post(new ReleasesChangedEvent());
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
