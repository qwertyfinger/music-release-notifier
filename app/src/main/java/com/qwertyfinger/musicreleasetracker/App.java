package com.qwertyfinger.musicreleasetracker;

import android.app.Application;
import android.util.Log;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.events.deezer.DeezerRequestFinEvent;
import com.qwertyfinger.musicreleasetracker.jobs.artist.AddArtistsJob;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;

public class App  extends Application{
    private static App instance;
    private JobManager jobManager;
    private DeezerConnect deezerConnect;
    public static boolean firstLoad = true;
    public static Random random = new Random();

    public App(){
        instance = this;
    }

    public static App getInstance() {
        return instance;
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
        configureJobManager();
        configureDeezerConnect();
        EventBus.getDefault().register(this);
    }

    private void configureDeezerConnect() {
        deezerConnect = new DeezerConnect(Constants.DEEZER_APP_ID);
        SessionStore sessionStore = new SessionStore();
        sessionStore.restore(deezerConnect, getApplicationContext());
    }

    private void configureJobManager() {
        Configuration config = new Configuration.Builder(this).customLogger(new CustomLogger() {
            private static final String TAG = "JOBS";
            @Override
            public boolean isDebugEnabled() {
                return false;
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
                    de.umass.lastfm.Artist.search(deezerArtist.getName(), Constants.LASTFM_API_KEY);

            for (de.umass.lastfm.Artist lastfmArtist: lastfmArtists){
                if (!lastfmArtist.getName().equalsIgnoreCase(deezerArtist.getName())
                        || lastfmArtist.getMbid().equals(""))
                    continue;

                String id = Utils.correctArtistMbid(lastfmArtist.getName());
                if (id == null)
                    id = lastfmArtist.getMbid();
                outputArtists.add(new com.qwertyfinger.musicreleasetracker.entities.Artist(id,
                        lastfmArtist.getName(),
                        lastfmArtist.getImageURL(ImageSize.EXTRALARGE)));
            }
        }

        jobManager.addJobInBackground(new AddArtistsJob(App.getInstance().getApplicationContext(), outputArtists));
    }
}
