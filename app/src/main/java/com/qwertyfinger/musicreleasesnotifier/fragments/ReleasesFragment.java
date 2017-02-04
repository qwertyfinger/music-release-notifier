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

package com.qwertyfinger.musicreleasesnotifier.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.adapters.ReleasesListAdapter;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistsChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasesnotifier.events.release.NoReleasesEvent;
import com.qwertyfinger.musicreleasesnotifier.events.release.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.release.ReleasesFetchedEvent;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.FetchReleasesJob;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.RefreshReleasesJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.ListScrollListener;

import de.greenrobot.event.EventBus;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.List;

public class ReleasesFragment extends Fragment {

    private JobManager jobManager;
    private ReleasesListAdapter mAdapter;
    private StickyListHeadersListView mStickyList;
    private TextView mNoReleases;
    private TextView mNoArtists;
    private Parcelable state;
    private List<Release> fetchedReleases;
    private DatabaseHandler db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        jobManager = App.getInstance().getJobManager();
        db = DatabaseHandler.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_releases, container, false);

        mNoArtists = (TextView) view.findViewById(R.id.noArtistsInRel);
        mNoReleases = (TextView) view.findViewById(R.id.noReleases);

        int artistCount = db.getArtistsCount();
        int releaseCount = db.getReleasesCount();

        if (artistCount == 0){
            mNoArtists.setVisibility(View.VISIBLE);
            mNoReleases.setVisibility(View.GONE);
        }
        else {
            if (releaseCount == 0) {
                mNoArtists.setVisibility(View.GONE);
                mNoReleases.setVisibility(View.VISIBLE);
            }
        }

        if (releaseCount != 0) {
            mNoArtists.setVisibility(View.GONE);
            mNoReleases.setVisibility(View.GONE);
        }

        mStickyList = (StickyListHeadersListView) view.findViewById(R.id.releasesList);
        mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
        mStickyList.setAreHeadersSticky(false);

        if (savedInstanceState != null){

            /*if (DatabaseHandler.getInstance(getActivity()).getArtistsCount() == 0)
                mNoArtists.setVisibility(View.VISIBLE);
            else
                mNoArtists.setVisibility(View.GONE);

            if (savedInstanceState.getInt("noReleasesVisibility") == View.VISIBLE)
                mNoReleases.setVisibility(View.VISIBLE);
            else
                mNoReleases.setVisibility(View.GONE);*/

            fetchedReleases = savedInstanceState.getParcelableArrayList("fetchedList");
            if (fetchedReleases == null)
                fetchedReleases = new ArrayList<>();
            if (fetchedReleases.size() > 0) {
                mAdapter = new ReleasesListAdapter(getActivity(), fetchedReleases);
                mStickyList.setAdapter(mAdapter);
            }
        } else {
            fetchedReleases = new ArrayList<>();
            /*if (DatabaseHandler.getInstance(getActivity()).getReleasesCount() != 0) {
                mNoArtists.setVisibility(View.GONE);
                mNoReleases.setVisibility(View.VISIBLE);
            }*/
        }

        if (state != null)
            mStickyList.onRestoreInstanceState(state);

        if (DatabaseHandler.getInstance(getActivity()).getReleasesCount() != fetchedReleases.size())
            jobManager.addJobInBackground(new FetchReleasesJob(getActivity()));

        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mStickyList != null)
            state = mStickyList.onSaveInstanceState();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mStickyList != null)
            state = mStickyList.onSaveInstanceState();

        /*if (mNoReleases != null)
            savedInstanceState.putInt("noReleasesVisibility", mNoReleases.getVisibility());

        if (mNoArtists != null)
            savedInstanceState.putInt("noArtistVisibility", mNoArtists.getVisibility());*/

        if (fetchedReleases != null)
            savedInstanceState.putParcelableArrayList("fetchedList", (ArrayList<Release>) fetchedReleases);

        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ArtistsChangedEvent event) {
        mNoArtists.setVisibility(View.GONE);
        if (db.getReleasesCount() == 0) {
            mNoReleases.setVisibility(View.VISIBLE);
        }
        if (event.getArtists() != null) {
            App.getInstance().getJobManager().addJobInBackground(new RefreshReleasesJob(getContext(), Constants
                    .AFTER_SYNC_REFRESH, event.getArtists()));
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ReleasesFetchedEvent event) {
        mNoReleases.setVisibility(View.GONE);
        mNoArtists.setVisibility(View.GONE);

        fetchedReleases = event.getReleases();

        mAdapter = new ReleasesListAdapter(getActivity(), event.getReleases());
        mStickyList.setAdapter(mAdapter);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ReleasesChangedEvent event) {
        jobManager.addJobInBackground(new FetchReleasesJob(getActivity()));

//        DatabaseHandler db = DatabaseHandler.getInstance(getActivity());

        /*Log.d("Reading: ", "Reading all releases..");
        Log.d("Count: ", db.getReleasesCount()+"");
        List<Release> list = db.getAllReleases();
        for (Release release : list) {
            String log = "Id: " + release.getId() + " ,Name: " + release.getTitle() + " ,ImageUrl: " + release.getImage() + " ,Artist: "
                    + release.getArtist() + " ,Date: " + release.getDate();
            Log.d("Release: ", log);
        }*/
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NoArtistsEvent event){
        mNoReleases.setVisibility(View.GONE);
        mNoArtists.setVisibility(View.VISIBLE);

        fetchedReleases = null;

        mAdapter = new ReleasesListAdapter(getActivity(), new ArrayList<Release>());
        mStickyList.setAdapter(mAdapter);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NoReleasesEvent event){
        mNoArtists.setVisibility(View.GONE);
        mNoReleases.setVisibility(View.VISIBLE);

        fetchedReleases = null;

        mAdapter = new ReleasesListAdapter(getActivity(), new ArrayList<Release>());
        mStickyList.setAdapter(mAdapter);
    }

}
