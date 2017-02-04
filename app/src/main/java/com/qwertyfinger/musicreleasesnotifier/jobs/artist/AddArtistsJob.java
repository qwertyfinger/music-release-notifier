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

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistAddedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistsChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class AddArtistsJob extends Job{

    private Context context;
    private final View view;
    private List<Artist> artists;
    private int actionId;

    public AddArtistsJob(Context c, List<Artist> artists, View view) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE).addTags(Constants.JOB_SYNC_TAG));
        this.context = c;
        this.artists = artists;
        this.view = view;
        actionId = Constants.ARTIST_USER_ADD;
    }

    public AddArtistsJob(Context c, List<Artist> artists) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE).addTags(Constants.JOB_SYNC_TAG));
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

        if (Utils.isConnected(context)) {

            final DatabaseHandler db = DatabaseHandler.getInstance(context);

            if (artists.size() == 0) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                        .SYNC_IN_PROGRESS, false).apply();
                EventBus.getDefault().post(new SyncFinishedEvent());
            }

            else {
                List<Artist> finalArtists = new ArrayList<>();

                for (final Artist artist : artists) {
                    if (!db.isArtistAdded(artist.getId()) && !isCancelled()) {
                        finalArtists.add(artist);
                    }
                }
                DatabaseHandler.getInstance(context).addArtists(finalArtists);

                if (actionId == Constants.ARTIST_USER_ADD) {
                    EventBus.getDefault().post(new ArtistAddedEvent(artists.get(0), view));
                    EventBus.getDefault().post(new ArtistsChangedEvent(null));
                } else {
                    EventBus.getDefault().post(new ArtistsChangedEvent(artists));
                }
            }
        } else {
                Utils.makeInternetToast(context);
        }
    }

    @Override
    protected void onCancel() {
    }

}
