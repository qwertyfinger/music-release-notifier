package com.qwertyfinger.musicreleasetracker.jobs;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.events.ReleaseAdapterEvent;
import com.qwertyfinger.musicreleasetracker.events.SearchQueryEvent;
import com.qwertyfinger.musicreleasetracker.events.SearchingEvent;

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

        List<com.qwertyfinger.musicreleasetracker.entities.Artist> finalArtists = new ArrayList<>();
        List<Artist> artists = (ArrayList<Artist>) Artist.search(query, Constants.LASTFM_API_KEY);

        for (Artist artist: artists){
            String id = artist.getMbid();
            if (!id.equals("")) {
                if (artist.getName().equalsIgnoreCase("muse"))
                    id = "9c9f1380-2516-4fc9-a3e6-f9f61941d090";
                if (artist.getName().equalsIgnoreCase("placebo"))
                    id = "847e8284-8582-4b0e-9c26-b042a4f49e57";
                String imageUrl = artist.getImageURL(ImageSize.EXTRALARGE);
                finalArtists.add(new com.qwertyfinger.musicreleasetracker.entities.Artist(id, artist.getName(), imageUrl));
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