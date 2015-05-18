package com.qwertyfinger.musicreleasetracker.jobs.sync;

import android.content.Context;
import android.preference.PreferenceManager;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.Utils;
import com.qwertyfinger.musicreleasetracker.events.sync.SyncInProgressEvent;
import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasetracker.jobs.artist.AddArtistsJob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Library;

public class SyncLastfmJob extends Job {

    private Context context;
    private int actionId;

    public SyncLastfmJob(Context context, int actionId) {
        super(new Params(Constants.JOB_PRIORITY_HIGH).requireNetwork().groupBy(Constants.JOB_GROUP_SYNC));
        this.context = context;
        this.actionId = actionId;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        if (!Utils.isSyncInProgress(context) && Utils.isExternalStorageWritable() && Utils.isConnected(context)) {
            EventBus.getDefault().post(new SyncInProgressEvent());

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                    true).commit();
            Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
            Caller.getInstance().setCache(null);

            String username = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.LAST_FM, "");
            int threshold = PreferenceManager.getDefaultSharedPreferences(context).getInt(SettingsFragment.LAST_FM_THRESHOLD, 0);

            Collection<Artist> lastfmArtists = Library.getAllArtists(username, Constants.LASTFM_API_KEY);
            if (lastfmArtists.size() > 0) {
                List<com.qwertyfinger.musicreleasetracker.entities.Artist> finalArtists = new ArrayList<>();

                for (Artist artist : lastfmArtists) {
                    if (artist.getPlaycount() < threshold)
                        break;
                    if (artist.getMbid().equals(""))
                        continue;
                    String id;
                    id = Utils.correctArtistMbid(artist.getName());
                    if (id == null)
                        id = artist.getMbid();
                    finalArtists.add(new com.qwertyfinger.musicreleasetracker.entities.Artist(id, artist.getName(), artist.getImageURL(ImageSize.EXTRALARGE)));
                }

                if (finalArtists.size() > 0)
                    App.getInstance().getJobManager().addJobInBackground(new AddArtistsJob(context, finalArtists));
            }
        }
        else {
            if (actionId == Constants.EXPLICIT_SYNC) {
                if (!Utils.isExternalStorageWritable())
                    Utils.makeExtStorToast(context);
                if (!Utils.isConnected(context))
                    Utils.makeInternetToast(context);
                if (Utils.isSyncInProgress(context))
                    Utils.makeSyncToast(context);
            }
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
