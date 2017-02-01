package com.qwertyfinger.musicreleasesnotifier.jobs.release;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.BuildConfig;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasesnotifier.events.release.ReleasesLoadedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncInProgressEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Album;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;

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

public class RefreshReleasesJob extends Job{

    private final Context context;
    private final int actionId;
    private List<Artist> addedArtists;
    private Target target;
    private final List<Release> resultForDatabase = new ArrayList<>();

    public RefreshReleasesJob(Context context, int actionId) {
        super(new Params(Constants.JOB_PRIORITY_MEDIUM).requireNetwork().groupBy(Constants.JOB_GROUP_DATABASE)
                .addTags(Constants.JOB_SYNC_TAG));
        this.context = context;
        this.actionId = actionId;
        addedArtists = null;
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


    if ((actionId == Constants.EXPLICIT_REFRESH || !Utils.isWifiOnly(context) || Utils.isWifiConnected(context))
        && Utils.isExternalStorageWritable() && (actionId == Constants.AFTER_SYNC_REFRESH || !Utils.isSyncInProgress
            (context))) {

        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (db.getArtistsCount() == 0 && actionId == Constants.EXPLICIT_REFRESH) {
            EventBus.getDefault().post(new NoArtistsEvent());
        }

        if (db.getArtistsCount() != 0) {
            EventBus.getDefault().post(new SyncInProgressEvent());

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                    true).commit();

            EventBus.getDefault().register(this);

            final List<Artist> artists;
            if (addedArtists != null)
                artists = addedArtists;
            else
                artists = db.getAllArtists();

            Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
            Caller.getInstance().setCache(null);

            int i = 0;

            StringBuilder sb = new StringBuilder("(");

            for (int k = 0; k < artists.size()-1; k++) {
                sb.append("arid:");
                sb.append(artists.get(k).getId());
                sb.append(" OR ");
            }

            sb.append("arid:");
            sb.append(artists.get(artists.size()-1).getId());
            sb.append(")");

            i++;
            Map<String, ReleaseWs2> map = new HashMap<>();
            Map<String, String> idMap = new HashMap<>();
            int year = Calendar.getInstance().get(Calendar.YEAR);
            org.musicbrainz.controller.Release release = new org.musicbrainz.controller.Release();

            release.search(sb + " AND primarytype:album AND status:official AND (date:" + (year++) + "-??-?? || date:"
                    + (year++)
                    + "-??-?? || date:" + (year) + "-??-??)");
            List<ReleaseResultWs2> results = release.getFullSearchResultList();

            for (ReleaseResultWs2 entry : results) {

                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int localYear = calendar.get(Calendar.YEAR);

                calendar.setTime(entry.getRelease().getDate());
                int releaseMonth = calendar.get(Calendar.MONTH);
                int releaseDay = calendar.get(Calendar.DAY_OF_MONTH);
                int releaseYear = calendar.get(Calendar.YEAR);

                String releaseType = entry.getRelease().getReleaseGroup().getTypeString();

                if (((releaseMonth > month || (releaseMonth == month && releaseDay >= day)) || releaseYear > localYear) &&
                        releaseType
                        .equalsIgnoreCase(Constants.TYPE_ALBUM)) {
                    if (!db.isReleaseAdded(entry.getRelease().getId())) {
                        if (!map.containsKey(entry.getRelease().getTitle())) {
                            map.put(entry.getRelease().getTitle(), entry.getRelease());
                            idMap.put(entry.getRelease().getTitle(), entry.getRelease().getArtistCreditString() /*artist.getId()*/);
                        } else {
                            if (map.get(entry.getRelease().getTitle()).getDate().after(entry.getRelease().getDate())) {
                                map.put(entry.getRelease().getTitle(), entry.getRelease());
                                idMap.put(entry.getRelease().getTitle(), entry.getRelease().getArtistCreditString() /*artist.getId()*/);
                            }
                        }
                    }
                }
            }

            if (map.isEmpty() && i == artists.size()) {
                EventBus.getDefault().post(new ReleasesLoadedEvent());
            }

            final Iterator<String> iterator = map.keySet().iterator();

            while (iterator.hasNext()) {
                String key = iterator.next();
                ReleaseWs2 entry = map.get(key);

                Album album = Album.getInfo(entry.getArtistCreditString(), entry.getTitle(), BuildConfig.LAST_FM_API_KEY);
                final String imageUrl = album.getImageURL(ImageSize.EXTRALARGE);
                final String filename = entry.getReleaseGroup().getId() + ".jpg";
                final int counter = i;

                DateFormat defaultFormatter = DateFormat.getDateInstance(DateFormat.LONG);

                resultForDatabase.add(new Release(entry.getReleaseGroup().getId(),
                        entry.getTitle(), entry.getArtistCreditString(),
                        defaultFormatter.format(map.get(key).getDate()), filename, idMap.get(key)));

                target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        FileOutputStream out = null;
                        try {
                            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                            out = new FileOutputStream(file);
                        } catch (FileNotFoundException e) {
                        }
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                        if (counter == artists.size())
                            EventBus.getDefault().post(new ReleasesLoadedEvent());
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
                                    .error(R.drawable.no_album_image)
                                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                                    .centerCrop()
                                    .tag(context)
                                    .into(target);
                        } catch (java.lang.IllegalArgumentException e) {
                            Picasso.with(context)
                                    .load(R.drawable.no_album_image)
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
                }
            }
        }
        else {
            if (actionId == Constants.EXPLICIT_REFRESH) {
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

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(ReleasesLoadedEvent event) {
        if (resultForDatabase != null) {
            JobManager jobManager = App.getInstance().getJobManager();

            if (!resultForDatabase.isEmpty()) {
                DatabaseHandler db = DatabaseHandler.getInstance(context);
                db.addReleases(resultForDatabase);
                jobManager.addJobInBackground(new FetchReleasesJob(context));
                jobManager.addJobInBackground(new ShowNotificationJob(context, resultForDatabase));
            }
            else
                jobManager.addJobInBackground(new FetchReleasesJob(context));

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                    .SYNC_IN_PROGRESS, false).commit();
            EventBus.getDefault().post(new SyncFinishedEvent());
            EventBus.getDefault().unregister(this);
        }
    }
}
