package com.qwertyfinger.musicreleasesnotifier.jobs.artist;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistAddedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistsChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class AddArtistsJob extends Job{

    private Context context;
    private final View view;
    private List<Artist> artists;
    private int actionId;

    public AddArtistsJob(Context c, List<Artist> artists, View view) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE).addTags(Constants.JOB_SYNC_TAG));
        this.context = c;
        this.artists = artists;
        this.view = view;
        actionId = Constants.ARTIST_USER_ADD;
    }

    public AddArtistsJob(Context c, List<Artist> artists) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE).addTags(Constants.JOB_SYNC_TAG));
        this.context = c;
        this.artists = artists;
        actionId = Constants.ARTIST_SYNC_ADD;
        this.view = null;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        if (Utils.isConnected(context)) {

            final DatabaseHandler db = DatabaseHandler.getInstance(context);

            if (artists.size() == 0) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                        .SYNC_IN_PROGRESS, false).apply();
                EventBus.getDefault().post(new SyncFinishedEvent());
            }

            else {
                List<Artist> finalArtists = new ArrayList<>();

                for (final Artist artist : artists) {
                    if (!db.isArtistAdded(artist.getId()) && !isCancelled()) {
                        finalArtists.add(artist);
                    }
                }
                DatabaseHandler.getInstance(context).addArtists(finalArtists);

                if (actionId == Constants.ARTIST_USER_ADD) {
                    EventBus.getDefault().post(new ArtistAddedEvent(artists.get(0), view));
                    EventBus.getDefault().post(new ArtistsChangedEvent(null));
                } else {
                    EventBus.getDefault().post(new ArtistsChangedEvent(artists));
                }
            }
        } else {
                Utils.makeInternetToast(context);
        }
    }

    @Override
    protected void onCancel() {
    }

}
