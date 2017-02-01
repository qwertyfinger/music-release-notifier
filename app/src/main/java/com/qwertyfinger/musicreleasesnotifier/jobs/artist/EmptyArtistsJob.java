package com.qwertyfinger.musicreleasesnotifier.jobs.artist;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.EmptyReleasesJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

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
        if (Utils.isExternalStorageWritable()  && !Utils.isSyncInProgress(context)) {
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
                Utils.makeToastNonUI(context, "Artists Emptied", Toast.LENGTH_SHORT);
            }
        } else {
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
