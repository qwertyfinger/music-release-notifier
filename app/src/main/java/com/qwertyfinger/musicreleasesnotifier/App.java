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

package com.qwertyfinger.musicreleasesnotifier;

import android.app.Application;
import android.util.Log;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.deezer.DeezerRequestFinEvent;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.AddArtistsJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App extends Application{
    private static App sInstance;
    private JobManager jobManager;
    private DeezerConnect deezerConnect;
//    public static boolean firstLoad = true;
    public static Random random = new Random();

    public static App getInstance() {
        return sInstance;
    }

    public JobManager getJobManager(){
        return jobManager;
    }

    public DeezerConnect getDeezerConnect() {
        return deezerConnect;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        if (sInstance == null) sInstance = this;
        configureJobManager();
        configureDeezerConnect();
        EventBus.getDefault().register(this);
    }

    private void configureDeezerConnect() {
        deezerConnect = new DeezerConnect(BuildConfig.DEEZER_APP_ID);
        SessionStore sessionStore = new SessionStore();
        sessionStore.restore(deezerConnect, getApplicationContext());
    }

    private void configureJobManager() {
        Configuration config = new Configuration.Builder(this).customLogger(new CustomLogger() {
            private static final String TAG = "JOBS";
            @Override
            public boolean isDebugEnabled() {
                return true;
            }

            @Override
            public void d(String text, Object... args) {
                Log.d(TAG, String.format(text, args));
            }

            @Override
            public void e(Throwable t, String text, Object... args) {
                Log.e(TAG, String.format(text, args), t);
            }

            @Override
            public void e(String text, Object... args) {
                Log.e(TAG, String.format(text, args));
            }
        })
        .build();
        jobManager = new JobManager(this, config);
    }



    @SuppressWarnings("unused")
    public void onEventAsync(DeezerRequestFinEvent event) {
        List<Artist> outputArtists = new ArrayList<>();

        Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
        Caller.getInstance().setCache(null);

        for (com.deezer.sdk.model.Artist deezerArtist: event.getArtists()) {
            List<de.umass.lastfm.Artist> lastfmArtists = (ArrayList<de.umass.lastfm.Artist>)
                    de.umass.lastfm.Artist.search(deezerArtist.getName(), BuildConfig.LAST_FM_API_KEY);

            for (de.umass.lastfm.Artist lastfmArtist: lastfmArtists){
                if (!lastfmArtist.getName().equalsIgnoreCase(deezerArtist.getName())
                        || lastfmArtist.getMbid().equals(""))
                    continue;

                String id = Utils.correctArtistMbid(lastfmArtist.getName());
                if (id == null)
                    id = lastfmArtist.getMbid();
                outputArtists.add(new com.qwertyfinger.musicreleasesnotifier.entities.Artist(id,
                        lastfmArtist.getName(),
                        lastfmArtist.getImageURL(ImageSize.EXTRALARGE)));
            }
        }

        jobManager.addJobInBackground(new AddArtistsJob(App.getInstance().getApplicationContext(), outputArtists));
    }
}
