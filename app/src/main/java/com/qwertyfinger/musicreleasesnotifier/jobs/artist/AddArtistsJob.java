package com.qwertyfinger.musicreleasesnotifier.jobs.artist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistAddedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistLoadedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistsChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.RefreshReleasesJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class AddArtistsJob extends Job{

    private Context context;
    private Target target[];
    private final View view;
    private List<Artist> artists;
    private int actionId;
    private List<Artist> finalArtists;

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

        if (Utils.isExternalStorageWritable() && Utils.isConnected(context)) {

            target = new Target[artists.size()];
            EventBus.getDefault().register(this);

            final DatabaseHandler db = DatabaseHandler.getInstance(context);

            finalArtists = new ArrayList<>();
            int counter = 0;
            for (final Artist artist : artists) {
                if (!db.isArtistAdded(artist.getId()) && !isCancelled()) {

                    final String imageUrl = artist.getImage();
                    final String filename = artist.getId() + ".jpg";

                    finalArtists.add(new Artist(artist.getId(), artist.getTitle(), filename));

                    target[counter] = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            FileOutputStream out = null;

                            try {
                                File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                                out = new FileOutputStream(file);
                            } catch (FileNotFoundException e) {
                            }

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);

                            EventBus.getDefault().post(new ArtistLoadedEvent());
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };

                    final int i = counter;
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Picasso.with(context)
                                        .load(imageUrl)
                                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                        .config(Bitmap.Config.RGB_565)
                                        .error(R.drawable.no_artist_image)
                                        .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                                        .centerCrop()
                                        .tag(context)
                                        .into(target[i]);
                            } catch (IllegalArgumentException e) {
                                Picasso.with(context)
                                        .load(R.drawable.no_artist_image)
                                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                        .config(Bitmap.Config.RGB_565)
                                        .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                                        .centerCrop()
                                        .tag(context)
                                        .into(target[i]);
                            }
                        }
                    });
                    counter++;

                }
                if (finalArtists.size() == 0) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                            .SYNC_IN_PROGRESS, false).commit();
                    EventBus.getDefault().post(new SyncFinishedEvent());
                }
            }
        } else {
            if (!Utils.isExternalStorageWritable())
                Utils.makeExtStorToast(context);
            if (!Utils.isConnected(context))
                Utils.makeInternetToast(context);
            if (EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onCancel() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        return false;
    }

    private int counter;
    @SuppressWarnings("unused")
    public void onEvent(ArtistLoadedEvent event) {
        if (actionId == Constants.ARTIST_USER_ADD) {
            List<Artist> list = new ArrayList<>(1);
            list.add(finalArtists.get(0));
            DatabaseHandler.getInstance(context).addArtists(list);
            EventBus.getDefault().post(new ArtistAddedEvent(finalArtists.get(0), view));
            EventBus.getDefault().unregister(this);
        }
        else {
            counter++;
            if (counter == finalArtists.size()) {
                DatabaseHandler.getInstance(context).addArtists(finalArtists);
                EventBus.getDefault().post(new ArtistsChangedEvent(finalArtists));

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    public void run() {
                        App.getInstance().getJobManager().addJobInBackground(new RefreshReleasesJob(context, Constants
                                .AFTER_SYNC_REFRESH, finalArtists));
                    }
                }, 1000);

                EventBus.getDefault().unregister(this);
            }
        }
    }
}
