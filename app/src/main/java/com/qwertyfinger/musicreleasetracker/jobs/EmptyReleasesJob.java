package com.qwertyfinger.musicreleasetracker.jobs;

import android.content.Context;
import android.os.Environment;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.entities.Release;
import com.qwertyfinger.musicreleasetracker.events.ReleasesChangedEvent;

import java.io.File;
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
                File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), release.getImage());
                image.delete();
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