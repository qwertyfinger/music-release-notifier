package com.qwertyfinger.musicreleasesnotifier.jobs.release;

import android.content.Context;
import android.preference.PreferenceManager;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.BuildConfig;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasesnotifier.events.release.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncInProgressEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Album;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;

import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseResultWs2;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefreshReleasesJob extends Job{

    private final Context context;
    private final int actionId;
    private List<Artist> addedArtists;
//    private Target target;
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

        int artistCount = db.getArtistsCount();

        if (artistCount == 0 && actionId == Constants.EXPLICIT_REFRESH) {
            EventBus.getDefault().post(new NoArtistsEvent());
        }

        if (artistCount != 0) {

            EventBus.getDefault().post(new SyncInProgressEvent());

            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                    true).apply();

            List<Artist> artists;
            if (addedArtists != null){
                artists = addedArtists;
            }
            else {
                artists = db.getAllArtists();
            }

            Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
            Caller.getInstance().setCache(null);



            List<ReleaseResultWs2> results = new ArrayList<>();

            StringBuilder sb = new StringBuilder();

            if (artists.size() == 1) {
                sb.append("(");
                sb.append("arid:");
                sb.append(artists.get(0).getId());
                sb.append(")");

                int year = Calendar.getInstance().get(Calendar.YEAR);
                org.musicbrainz.controller.Release release = new org.musicbrainz.controller.Release();

                release.search(sb + " AND primarytype:album AND status:official AND (date:" + (year) + "-??-?? || date:"
                        + (++year)
                        + "-??-?? || date:" + (++year) + "-??-??)");

                results.addAll(release.getFullSearchResultList());

            } else {
                for (int i = 0; i < artists.size(); i += 25) {
                    sb.append("(");

                    for (int k = i; k < i+24; k++) {

                        if (k >= artists.size()) break;

                        sb.append("arid:");
                        sb.append(artists.get(k).getId());
                        sb.append(" OR ");
                    }

                    sb.append("arid:");
                    sb.append(artists.get(artists.size()-1).getId());
                    sb.append(")");

                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    org.musicbrainz.controller.Release release = new org.musicbrainz.controller.Release();

                    release.search(sb + " AND primarytype:album AND status:official AND (date:" + (year) + "-??-?? || date:"
                            + (++year)
                            + "-??-?? || date:" + (++year) + "-??-??)");

                    results.addAll(release.getFullSearchResultList());
                    sb.setLength(0);
                }
            }

            Map<String, ReleaseWs2> map = new HashMap<>();
            Map<String, String> idMap = new HashMap<>();

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

            if (!map.isEmpty()) {

                for (String key : map.keySet()) {
                    ReleaseWs2 entry = map.get(key);

                    Album album = Album.getInfo(entry.getArtistCreditString(), entry.getTitle(), BuildConfig.LAST_FM_API_KEY);
                    final String imageUrl = album.getImageURL(ImageSize.EXTRALARGE);
                    //                final int counter = i;

                    DateFormat defaultFormatter = DateFormat.getDateInstance(DateFormat.LONG);

                    resultForDatabase.add(new Release(entry.getReleaseGroup().getId(),
                            entry.getTitle(), entry.getArtistCreditString(),
                            defaultFormatter.format(map.get(key).getDate()), imageUrl, idMap.get(key)));
                }
            }

            if (!resultForDatabase.isEmpty()) {
                db.addReleases(resultForDatabase);
                EventBus.getDefault().post(new ReleasesChangedEvent());
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                    .SYNC_IN_PROGRESS, false).apply();
            EventBus.getDefault().post(new SyncFinishedEvent());
            }
        } else {
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

}
