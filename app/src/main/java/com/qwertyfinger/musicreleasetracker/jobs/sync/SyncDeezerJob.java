package com.qwertyfinger.musicreleasetracker.jobs.sync;

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.events.deezer.DeezerRequestFinEvent;

import java.util.List;

import de.greenrobot.event.EventBus;

public class SyncDeezerJob extends Job {

    public SyncDeezerJob() {
        super(new Params(Constants.JOB_PRIORITY_HIGH).requireNetwork());
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        RequestListener listener = new JsonRequestListener() {
            @Override
            public void onResult(Object result, Object o1) {
                List<Artist> deezerArtists = (List<Artist>) result;
                if (deezerArtists.size() > 0)
                    EventBus.getDefault().post(new DeezerRequestFinEvent(deezerArtists));
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
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
