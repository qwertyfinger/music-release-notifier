package com.qwertyfinger.musicreleasestracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasestracker.App;
import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.adapters.ReleasesListAdapter;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasestracker.events.NoReleasesEvent;
import com.qwertyfinger.musicreleasestracker.events.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasestracker.events.ReleasesFetchedEvent;
import com.qwertyfinger.musicreleasestracker.jobs.FetchReleasesJob;
import com.qwertyfinger.musicreleasestracker.misc.ListScrollListener;
import com.qwertyfinger.musicreleasestracker.misc.Release;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ReleasesFragment extends Fragment {

    private JobManager jobManager;
    private ReleasesListAdapter mAdapter;
    private StickyListHeadersListView mStickyList;
    private TextView mNoReleases;
    private TextView mNoArtists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        jobManager = App.getInstance().getJobManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_releases, container, false);

        mNoArtists = (TextView) view.findViewById(R.id.noArtists);
        mNoReleases = (TextView) view.findViewById(R.id.noReleases);
        mNoArtists.setVisibility(View.GONE);

        jobManager.addJobInBackground(new FetchReleasesJob(getActivity()));

        mStickyList = (StickyListHeadersListView) view.findViewById(R.id.list);
        mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
        mStickyList.setAreHeadersSticky(false);

        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ReleasesFetchedEvent event) {
        mNoReleases.setVisibility(View.GONE);
        mNoArtists.setVisibility(View.GONE);

        mAdapter = new ReleasesListAdapter(getActivity(), event.getReleases());
        mStickyList.setAdapter(mAdapter);
    }

    public void onEventMainThread(ReleasesChangedEvent event) {
        jobManager.addJobInBackground(new FetchReleasesJob(getActivity()));

        DatabaseHandler db = DatabaseHandler.getInstance(getActivity());

        Log.d("Reading: ", "Reading all releases..");
        Log.d("Count: ", db.getReleasesCount()+"");
        List<Release> list = db.getAllReleases();
        for (Release release : list) {
            String log = "Id: " + release.getId() + " ,Name: " + release.getTitle() + " ,ImageUrl: " + release.getImage() + " ,Artist: "
                    + release.getArtist() + " ,Date: " + release.getDate();
            Log.d("Release: ", log);
        }
    }

    public void onEventMainThread(NoArtistsEvent event){
        mNoReleases.setVisibility(View.GONE);
        mNoArtists.setVisibility(View.VISIBLE);

        mAdapter = new ReleasesListAdapter(getActivity(), new ArrayList<Release>());
        mStickyList.setAdapter(mAdapter);
    }

    public void onEventMainThread(NoReleasesEvent event){
        mNoArtists.setVisibility(View.GONE);
        mNoReleases.setVisibility(View.VISIBLE);

        mAdapter = new ReleasesListAdapter(getActivity(), new ArrayList<Release>());
        mStickyList.setAdapter(mAdapter);
    }

}
