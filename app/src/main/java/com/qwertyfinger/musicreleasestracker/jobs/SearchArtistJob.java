package com.qwertyfinger.musicreleasestracker.jobs;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.events.SearchQueryEvent;
import com.qwertyfinger.musicreleasestracker.events.SearchingEvent;
import com.qwertyfinger.musicreleasestracker.misc.SearchResult;

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
    }

    @Override
    public void onRun() throws Throwable {
        Caller.getInstance().setUserAgent("tst");
        Caller.getInstance().setCache(null);

        List<SearchResult> searchResults = new ArrayList<>();
        List<Artist> artists = (ArrayList<Artist>) Artist.search(query, Constants.LASTFM_API_KEY);

        for (Artist artist: artists){
            String mbid = artist.getMbid();
            if (!mbid.equals("")) {
                String imageUrl = artist.getImageURL(ImageSize.EXTRALARGE);
                searchResults.add(new SearchResult(artist.getName(), imageUrl, mbid));
            }
        }
        EventBus.getDefault().post(new SearchQueryEvent(searchResults));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
