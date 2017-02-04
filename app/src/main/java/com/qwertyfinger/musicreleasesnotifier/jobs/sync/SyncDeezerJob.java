/*
 * MIT License
 *
 * Copyright (c) 2017 Andriy Chubko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.qwertyfinger.musicreleasesnotifier.jobs.sync;

import android.content.Context;
import android.preference.PreferenceManager;

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.events.deezer.DeezerRequestFinEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncInProgressEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;

import java.util.List;

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
                    true).apply();
            RequestListener listener = new JsonRequestListener() {
                @Override
                public void onResult(Object result, Object o1) {
                    List<Artist> deezerArtists = (List<Artist>) result;
                    if (deezerArtists.size() > 0)
                        EventBus.getDefault().post(new DeezerRequestFinEvent(deezerArtists));
                    else  {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                                false).apply();
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
