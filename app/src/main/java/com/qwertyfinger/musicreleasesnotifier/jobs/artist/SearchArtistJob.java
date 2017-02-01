package com.qwertyfinger.musicreleasesnotifier.jobs.artist;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.events.ReleaseAdapterEvent;
import com.qwertyfinger.musicreleasesnotifier.events.search.SearchQueryEvent;
import com.qwertyfinger.musicreleasesnotifier.events.search.SearchingEvent;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;

public class SearchArtistJob extends Job {

    private final String query;

    public SearchArtistJob(String query) {
        super(new Params(Constants.JOB_PRIORITY_CRITICAL).requireNetwork());
        this.query = query;
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new SearchingEvent());
        EventBus.getDefault().post(new ReleaseAdapterEvent());
    }

    @Override
    public void onRun() throws Throwable {
        Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
        Caller.getInstance().setCache(null);

        List<com.qwertyfinger.musicreleasesnotifier.entities.Artist> finalArtists = new ArrayList<>();
        List<Artist> artists = (ArrayList<Artist>) Artist.search(query, Constants.LASTFM_API_KEY);

        for (Artist artist: artists){
            String id = artist.getMbid();
            if (!id.equals("")) {
                id = Utils.correctArtistMbid(artist.getName());
                if (id == null)
                    id = artist.getMbid();
                String imageUrl = artist.getImageURL(ImageSize.EXTRALARGE);
                finalArtists.add(new com.qwertyfinger.musicreleasesnotifier.entities.Artist(id, artist.getName(), imageUrl));
            }
        }
        EventBus.getDefault().post(new SearchQueryEvent(finalArtists));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
