package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasestracker.events.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.qwertyfinger.musicreleasestracker.misc.Release;

import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseResultWs2;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class RefreshReleasesJob extends Job{

    private final Context context;
    private final int actionId;

    public RefreshReleasesJob(Context context, int actionId) {
        super(new Params(1).requireNetwork().groupBy("database"));
        this.context = context;
        this.actionId = actionId;
    }


    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (db.getArtistsCount() == 0 && actionId != Constants.SCHEDULED_REFRESH) {
            EventBus.getDefault().post(new NoArtistsEvent());
        }

        if (db.getArtistsCount() != 0) {
            List<Artist> artists = db.getAllArtists();
            List<Release> resultForDatabase = new ArrayList<>();

            for (Artist artist: artists) {
                Map<String, ReleaseWs2> map = new HashMap<>();
                int year = Calendar.getInstance().get(Calendar.YEAR);
                org.musicbrainz.controller.Release release = new org.musicbrainz.controller.Release();

                release.search("arid:" + artist.getId() + " AND primarytype:album AND status:official AND (date:" + (year++) + "-??-?? || date:" + (year++)
                        + "-??-?? || date:" + (year) + "-??-??)");
                List<ReleaseResultWs2> results = release.getFullSearchResultList();

                for (ReleaseResultWs2 entry: results) {

                    Calendar calendar = Calendar.getInstance();
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    calendar.setTime(entry.getRelease().getDate());
                    int releaseMonth = calendar.get(Calendar.MONTH);
                    int releaseDay = calendar.get(Calendar.DAY_OF_MONTH);

                    String releaseType = entry.getRelease().getReleaseGroup().getTypeString();

                    if ((releaseMonth > month || (releaseMonth == month && releaseDay >= day)) && releaseType.equalsIgnoreCase(Constants.TYPE_ALBUM)) {
                        if (!map.containsKey(entry.getRelease().getTitle()))
                            map.put(entry.getRelease().getTitle(), entry.getRelease());
                        else {
                            if (map.get(entry.getRelease().getTitle()).getDate().after(entry.getRelease().getDate()))
                                map.put(entry.getRelease().getTitle(), entry.getRelease());
                        }
                    }
                }

                Iterator<String> iterator = map.keySet().iterator();

                while (iterator.hasNext()) {
                    String key = iterator.next();
                    ReleaseWs2 entry = map.get(key);

                    DateFormat defaultFormatter = DateFormat.getDateInstance(DateFormat.LONG);

                    resultForDatabase.add(new Release(entry.getReleaseGroup().getId(), entry.getTitle(), entry.getArtistCreditString(), defaultFormatter.format(map.get(key).getDate()), ""));
                }
                results.clear();
                map.clear();
            }

            db.addReleases(resultForDatabase);
            if (actionId == Constants.EXPLICIT_REFRESH) EventBus.getDefault().post(new ReleasesChangedEvent(Constants.EXPLICIT_REFRESH));
            if (actionId == Constants.AFTER_ADDING_REFRESH) EventBus.getDefault().post(new ReleasesChangedEvent(Constants.AFTER_ADDING_REFRESH));
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
