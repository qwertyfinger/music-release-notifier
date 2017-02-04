/*
 * MIT License
 *
 * Copyright (c) 2017 Andriy Chubko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.qwertyfinger.musicreleasesnotifier.jobs.artist;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.BuildConfig;
import com.qwertyfinger.musicreleasesnotifier.events.ReleaseAdapterEvent;
import com.qwertyfinger.musicreleasesnotifier.events.search.SearchQueryEvent;
import com.qwertyfinger.musicreleasesnotifier.events.search.SearchingEvent;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.ImageSize;

import java.util.ArrayList;
import java.util.List;

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
        List<Artist> artists = (ArrayList<Artist>) Artist.search(query, BuildConfig.LAST_FM_API_KEY);

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
