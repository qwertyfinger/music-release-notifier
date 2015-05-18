package com.qwertyfinger.musicreleasetracker.jobs.sync;

import android.content.Context;
import android.preference.PreferenceManager;

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.Utils;
import com.qwertyfinger.musicreleasetracker.events.deezer.DeezerRequestFinEvent;
import com.qwertyfinger.musicreleasetracker.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasetracker.events.sync.SyncInProgressEvent;
import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;

import java.util.List;

import de.greenrobot.event.EventBus;

public class SyncDeezerJob extends Job {

    private Context context;
    private int actionId;

    public SyncDeezerJob(Context context, int actionId) {
        super(new Params(Constants.JOB_PRIORITY_HIGH).requireNetwork().groupBy(Constants.JOB_GROUP_SYNC));
        this.context = context;
        this.actionId = actionId;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        if (!Utils.isSyncInProgress(context)  && Utils.isExternalStorageWritable() && Utils.isConnected(context)) {
            EventBus.getDefault().post(new SyncInProgressEvent());

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                    true).commit();
            RequestListener listener = new JsonRequestListener() {
                @Override
                public void onResult(Object result, Object o1) {
                    List<Artist> deezerArtists = (List<Artist>) result;
                    if (deezerArtists.size() > 0)
                        EventBus.getDefault().post(new DeezerRequestFinEvent(deezerArtists));
                    else  {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                                false).commit();
                        EventBus.getDefault().post(new SyncFinishedEvent());
                    }
                }

                @Override
                public void onUnparsedResult(String s, Object o) {

                }

                @Override
                public void onException(Exception e, Object o) {

                }
            };

            DeezerRequest request = DeezerRequestFactory.requestCurrentUserArtists();
            App.getInstance().getDeezerConnect().requestAsync(request, listener);
        } else {
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
