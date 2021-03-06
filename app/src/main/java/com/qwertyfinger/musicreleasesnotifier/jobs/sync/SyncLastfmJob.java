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

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.BuildConfig;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncInProgressEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.AddArtistsJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SyncLastfmJob extends Job {

    private Context context;
    private int actionId;
    private int i;
    private List<com.qwertyfinger.musicreleasesnotifier.entities.Artist> finalArtists;

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
        if (!Utils.isSyncInProgress(context) && Utils.isConnected(context)) {
            EventBus.getDefault().post(new SyncInProgressEvent());

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                    true).apply();
            Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
            Caller.getInstance().setCache(null);

            String username = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.LAST_FM, "");
            int threshold = PreferenceManager.getDefaultSharedPreferences(context).getInt(SettingsFragment.LAST_FM_THRESHOLD, 0);

            Collection<Artist> lastfmArtists = Library.getAllArtists(username, BuildConfig.LAST_FM_API_KEY);
            if (lastfmArtists.size() > 0) {
                finalArtists = new ArrayList<>();

                for (Artist artist : lastfmArtists) {
                    if (artist.getPlaycount() < threshold)
                        break;
                    if (artist.getMbid().equals(""))
                        continue;
                    String id;
                    id = Utils.correctArtistMbid(artist.getName());
                    if (id == null)
                        id = artist.getMbid();
                    finalArtists.add(new com.qwertyfinger.musicreleasesnotifier.entities.Artist(id, artist.getName(), artist.getImageURL(ImageSize.EXTRALARGE)));
                }

                if (finalArtists.size() > 0) {
                    App.getInstance().getJobManager().addJobInBackground(new AddArtistsJob(context, finalArtists));
                    /*EventBus.getDefault().register(this);
                    List<com.qwertyfinger.musicreleasesnotifier.entities.Artist> partList = new ArrayList<>();
                    for (int k = 0; k < finalArtists.size(); k++) {
                        if (i > finalArtists.size()) {
                            EventBus.getDefault().unregister(this);
                            break;
                        }
                        partList.add(finalArtists.get(i));
                        i++;
                    }
                    App.getInstance().getJobManager().addJobInBackground(new AddArtistsJob(context, partList));*/
                }
            }
        }
        else {
            if (actionId == Constants.EXPLICIT_SYNC) {
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

    /*@SuppressWarnings("unused")
    *//*public void onEvent(SyncFinishedEvent event) {
        List<com.qwertyfinger.musicreleasesnotifier.entities.Artist> partList = new ArrayList<>();
        for (int k = 0; k < 50; k++) {
            if (i >= finalArtists.size()) {
                EventBus.getDefault().unregister(this);
                break;
            }
            partList.add(finalArtists.get(i));
            i++;
        }
        App.getInstance().getJobManager().addJobInBackground(new AddArtistsJob(context, partList));
    }*/
}
