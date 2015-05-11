package com.qwertyfinger.musicreleasetracker.jobs;

import android.content.Context;
import android.os.Environment;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasetracker.misc.Utils;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

public class EmptyArtistsJob extends Job {

    private final Context context;
    private JobManager jobManager;

    public EmptyArtistsJob(Context context) {
        super(new Params(Constants.JOB_PRIORITY_HIGH).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
        jobManager = App.getInstance().getJobManager();
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        if (Utils.isExternalStorageWritable()) {
            DatabaseHandler db = DatabaseHandler.getInstance(context);

            if (db.getArtistsCount() > 0) {
                List<Artist> artists = db.getAllArtists();
                for (Artist artist: artists){
                    File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), artist.getImage());
                    image.delete();
                }

                db.deleteAllArtists();
                EventBus.getDefault().post(new NoArtistsEvent());
                jobManager.addJobInBackground(new EmptyReleasesJob(context));
            }
        } else
            Utils.makeExtStorToast(context);
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
