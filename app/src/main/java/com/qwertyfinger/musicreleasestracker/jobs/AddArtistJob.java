package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ArtistAddedEvent;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.qwertyfinger.musicreleasestracker.misc.SearchResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.greenrobot.event.EventBus;

public class AddArtistJob extends Job{

    private final Context context;
    private final SearchResult searchResult;
    private Target target;

    public AddArtistJob(Context c, SearchResult searchResult) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = c;
        this.searchResult = searchResult;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        final DatabaseHandler db = DatabaseHandler.getInstance(context);
        final Artist artist = new Artist(searchResult.getId(), searchResult.getTitle(), searchResult.getImageUrl());
        final String imageUrl = artist.getImage();
        final String filename = artist.getId() + ".jpg";

        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                FileOutputStream out = null;
                try {
                    out = context.openFileOutput(filename, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                artist.setImage(filename);
                EventBus.getDefault().post(new ArtistAddedEvent(artist));
                db.addArtist(artist);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Picasso.with(context)
                            .load(imageUrl)
                            .config(Bitmap.Config.RGB_565)
                            .error(R.drawable.no_image)
                            .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                            .centerCrop()
                            .tag(context)
                            .into(target);
                } catch (java.lang.IllegalArgumentException e) {
                    Picasso.with(context)
                            .load(R.drawable.no_image)
                            .config(Bitmap.Config.RGB_565)
                            .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                            .centerCrop()
                            .tag(context)
                            .into(target);
                }
            }
        });
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
