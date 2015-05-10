package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;
import android.view.View;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasestracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasestracker.events.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasestracker.misc.Artist;

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
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.deleteArtist(artist.getId());
        if (db.isThereReleaseByArtist(artist.getId())) {
            db.deleteReleasesByArtist(artist.getId());
            EventBus.getDefault().post(new ReleasesChangedEvent());
        }
        context.deleteFile(artist.getId() + ".jpg");
        EventBus.getDefault().post(new ArtistDeletedEvent(view, artist));
        if (db.getArtistsCount() == 0)
            EventBus.getDefault().post(new NoArtistsEvent());
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
