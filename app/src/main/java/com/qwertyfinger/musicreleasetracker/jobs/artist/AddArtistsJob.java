package com.qwertyfinger.musicreleasetracker.jobs.artist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.Utils;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.events.artist.ArtistAddedEvent;
import com.qwertyfinger.musicreleasetracker.events.artist.ArtistLoadedEvent;
import com.qwertyfinger.musicreleasetracker.events.artist.ArtistsChangedEvent;
import com.qwertyfinger.musicreleasetracker.jobs.release.RefreshReleasesJob;
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
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = c;
        this.artists = artists;
        this.view = view;
        actionId = Constants.ARTIST_USER_ADD;
    }

    public AddArtistsJob(Context c, List<Artist> artists) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE));
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
            for(final Artist artist: artists) {
                if (!db.isArtistAdded(artist.getId())) {
                    final String imageUrl = artist.getImage();
                    final String filename = artist.getId() + ".jpg";


                    target[counter] = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            FileOutputStream out = null;

                            try {
                                File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                                out = new FileOutputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);

                            if (actionId == Constants.ARTIST_USER_ADD) {
                                List<Artist> oneArtist = new ArrayList<>();
                                oneArtist.add(new Artist(artist.getId(), artist.getTitle(), filename));
                                db.addArtists(oneArtist);
                                EventBus.getDefault().post(new ArtistAddedEvent(artist, view));
                            } else
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
                                        .error(R.drawable.no_image)
                                        .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                                        .centerCrop()
                                        .tag(context)
                                        .into(target[i]);
                            } catch (IllegalArgumentException e) {
                                Picasso.with(context)
                                        .load(R.drawable.no_image)
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
                    finalArtists.add(new Artist(artist.getId(), artist.getTitle(), filename));
                }
            }
        }
        else {
            if (!Utils.isConnected(context))
                Utils.makeInternetToast(context);
            if (!Utils.isExternalStorageWritable())
                Utils.makeExtStorToast(context);
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
    public void onEvent(ArtistLoadedEvent event) {
        counter++;
        if (counter == finalArtists.size()) {
            DatabaseHandler.getInstance(context).addArtists(finalArtists);
            EventBus.getDefault().post(new ArtistsChangedEvent());
            EventBus.getDefault().unregister(this);
            App.getInstance().getJobManager().addJobInBackground(new RefreshReleasesJob(context, Constants.AFTER_ADDING_REFRESH, finalArtists));
        }
    }
}
