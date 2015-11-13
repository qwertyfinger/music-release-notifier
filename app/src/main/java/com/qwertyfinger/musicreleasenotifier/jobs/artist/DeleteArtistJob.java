package com.qwertyfinger.musicreleasetracker.jobs.artist;

import android.content.Context;
import android.os.Environment;
import android.view.View;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.entities.Release;
import com.qwertyfinger.musicreleasetracker.events.artist.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasetracker.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasetracker.events.release.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasetracker.misc.Constants;
import com.qwertyfinger.musicreleasetracker.misc.Utils;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DeleteArtistJob extends Job {

    private final Context context;
    private final Artist artist;
    private final View view;

    public DeleteArtistJob(Context c, Artist artist, View view) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = c;
        this.artist = artist;
        this.view = view;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        if (Utils.isExternalStorageWritable()  && !Utils.isSyncInProgress(context)) {
            DatabaseHandler db = DatabaseHandler.getInstance(context);
            db.deleteArtist(artist.getId());
            List<Release> releases = db.getReleasesByArtist(artist.getId());
            if (!releases.isEmpty()) {
                db.deleteReleasesByArtist(artist.getId());

                for (Release release: releases){
                    File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), release.getImage());
                    image.delete();
                }

                EventBus.getDefault().post(new ReleasesChangedEvent());
            }

            File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), artist.getImage());
            image.delete();

            EventBus.getDefault().post(new ArtistDeletedEvent(view, artist));
            if (db.getArtistsCount() == 0)
                EventBus.getDefault().post(new NoArtistsEvent());
        }
        else {
            if (!Utils.isExternalStorageWritable())
                Utils.makeExtStorToast(context);
            if (Utils.isSyncInProgress(context))
                Utils.makeSyncToast(context);
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
