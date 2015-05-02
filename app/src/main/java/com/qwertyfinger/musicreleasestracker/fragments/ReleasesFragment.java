package com.qwertyfinger.musicreleasestracker.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ReleasesFragment extends Fragment {

    private JobManager jobManager;
    private ReleasesListAdapter mAdapter;
    private StickyListHeadersListView mStickyList;
    private TextView mNoReleases;
    private TextView mNoArtists;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        jobManager = App.getInstance().getJobManager();
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
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
        try {
            EventBus.getDefault().unregister(this);
        }
        catch (Throwable t){
            //in case registration didn't go through
        }
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
