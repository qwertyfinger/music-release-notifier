package com.qwertyfinger.musicreleasesnotifier.jobs.artist;

import android.content.Context;
import android.view.View;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasesnotifier.events.release.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;

import java.util.List;

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

        if (/*Utils.isExternalStorageWritable()  &&*/ !Utils.isSyncInProgress(context)) {
            DatabaseHandler db = DatabaseHandler.getInstance(context);
            db.deleteArtist(artist.getId());
            List<Release> releases = db.getReleasesByArtist(artist.getTitle());
            if (!releases.isEmpty()) {
                db.deleteReleasesByArtist(artist.getTitle());

                EventBus.getDefault().post(new ReleasesChangedEvent());
            }

            EventBus.getDefault().post(new ArtistDeletedEvent(view, artist));
            if (db.getArtistsCount() == 0)
                EventBus.getDefault().post(new NoArtistsEvent());
        }
        else {
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
