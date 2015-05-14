package com.qwertyfinger.musicreleasetracker.jobs.release;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.entities.Release;
import com.qwertyfinger.musicreleasetracker.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasetracker.events.release.ReleasesLoadedEvent;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseResultWs2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Album;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;

public class RefreshReleasesJob extends Job{

    private final Context context;
    private final int actionId;
    private List<Artist> addedArtists = null;
    private Target target;

    public RefreshReleasesJob(Context context, int actionId) {
        super(new Params(Constants.JOB_PRIORITY_LOW).requireNetwork().groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
        this.actionId = actionId;
    }

    public RefreshReleasesJob(Context context, int actionId, List<Artist> addedArtists) {
        super(new Params(Constants.JOB_PRIORITY_LOW).requireNetwork().groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
        this.actionId = actionId;
        this.addedArtists = addedArtists;
    }


    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

//          does not work on emulator

            /*if ((actionId == Constants.EXPLICIT_REFRESH ||
                !Utils.isWifiOnly(context)
                || Utils.isWifiConnected(context))
                && Utils.isExternalStorageWritable()) {*/


        DatabaseHandler db = DatabaseHandler.getInstance(context);

            if (db.getArtistsCount() == 0 && actionId == Constants.EXPLICIT_REFRESH) {
                EventBus.getDefault().post(new NoArtistsEvent());
            }
            if (db.getArtistsCount() != 0) {
                final List<Artist> artists;
                if (actionId == Constants.AFTER_ADDING_REFRESH && addedArtists != null)
                    artists = addedArtists;
                else
                    artists = db.getAllArtists();

                Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
                Caller.getInstance().setCache(null);

                final List<Release> resultForDatabase = new ArrayList<>();
                int i = 0;
                for (Artist artist : artists) {
                    i++;
                    Map<String, ReleaseWs2> map = new HashMap<>();
                    Map<String, String> idMap = new HashMap<>();
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    org.musicbrainz.controller.Release release = new org.musicbrainz.controller.Release();

                    release.search("arid:" + artist.getId() + " AND primarytype:album AND status:official AND (date:" + (year++) + "-??-?? || date:" + (year++)
                            + "-??-?? || date:" + (year) + "-??-??)");
                    final List<ReleaseResultWs2> results = release.getFullSearchResultList();

                    for (ReleaseResultWs2 entry : results) {

                        Calendar calendar = Calendar.getInstance();
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        calendar.setTime(entry.getRelease().getDate());
                        int releaseMonth = calendar.get(Calendar.MONTH);
                        int releaseDay = calendar.get(Calendar.DAY_OF_MONTH);

                        String releaseType = entry.getRelease().getReleaseGroup().getTypeString();

                        if ((releaseMonth > month || (releaseMonth == month && releaseDay >= day)) && releaseType.equalsIgnoreCase(Constants.TYPE_ALBUM)) {
                            if (!db.isReleaseAdded(entry.getRelease().getId())) {
                                if (!map.containsKey(entry.getRelease().getTitle())) {
                                    map.put(entry.getRelease().getTitle(), entry.getRelease());
                                    idMap.put(entry.getRelease().getTitle(), artist.getId());
                                } else {
                                    if (map.get(entry.getRelease().getTitle()).getDate().after(entry.getRelease().getDate())) {
                                        map.put(entry.getRelease().getTitle(), entry.getRelease());
                                        idMap.put(entry.getRelease().getTitle(), artist.getId());
                                    }
                                }
                            }
                        }
                    }

                    if (map.isEmpty() && i == artists.size()) {
                        EventBus.getDefault().post(new ReleasesLoadedEvent(resultForDatabase));
                        return;
                    }

                    final Iterator<String> iterator = map.keySet().iterator();

                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        ReleaseWs2 entry = map.get(key);

                        Album album = Album.getInfo(entry.getArtistCreditString(), entry.getTitle(), Constants.LASTFM_API_KEY);
                        final String imageUrl = album.getImageURL(ImageSize.EXTRALARGE);
                        final String filename = entry.getReleaseGroup().getId() + ".jpg";
                        final int counter = i;

                        target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                FileOutputStream out = null;
                                try {
                                    File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                                    out = new FileOutputStream(file);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                                if (counter == artists.size())
                                    EventBus.getDefault().post(new ReleasesLoadedEvent(resultForDatabase));
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
                                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                            .config(Bitmap.Config.RGB_565)
                                            .error(R.drawable.no_image)
                                            .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                                            .centerCrop()
                                            .tag(context)
                                            .into(target);
                                } catch (java.lang.IllegalArgumentException e) {
                                    Picasso.with(context)
                                            .load(R.drawable.no_image)
                                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                            .config(Bitmap.Config.RGB_565)
                                            .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                                            .centerCrop()
                                            .tag(context)
                                            .into(target);
                                }
                            }
                        });

                        DateFormat defaultFormatter = DateFormat.getDateInstance(DateFormat.LONG);

                        resultForDatabase.add(new Release(entry.getReleaseGroup().getId(), entry.getTitle(), entry.getArtistCreditString(),
                                defaultFormatter.format(map.get(key).getDate()), filename, idMap.get(key)));
                    }
                }
            }
//        }
        /*else {
            if (actionId == Constants.EXPLICIT_REFRESH && !Utils.isExternalStorageWritable())
                Utils.makeExtStorToast(context);
        }*/
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }


}
