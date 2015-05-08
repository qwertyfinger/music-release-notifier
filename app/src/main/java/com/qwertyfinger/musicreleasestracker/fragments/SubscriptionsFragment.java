package com.qwertyfinger.musicreleasestracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasestracker.App;
import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.adapters.ArtistsListAdapter;
import com.qwertyfinger.musicreleasestracker.events.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasestracker.events.ArtistsFetchedEvent;
import com.qwertyfinger.musicreleasestracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasestracker.jobs.FetchArtistsJob;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.qwertyfinger.musicreleasestracker.misc.ListScrollListener;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class SubscriptionsFragment extends Fragment{

    private JobManager jobManager;
    private ArtistsListAdapter mAdapter;
    private StickyListHeadersListView mStickyList;
    private TextView mNoArtists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        jobManager = App.getInstance().getJobManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);

        mNoArtists = (TextView) view.findViewById(R.id.noArtists);

        jobManager.addJobInBackground(new FetchArtistsJob(getActivity()));

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

    public void onEventMainThread(ArtistsFetchedEvent event){
        mNoArtists.setVisibility(View.GONE);

        mAdapter = new ArtistsListAdapter(getActivity(), event.getArtists());
        mStickyList.setAdapter(mAdapter);
    }

    public void onEventMainThread(NoArtistsEvent event){
        mNoArtists.setVisibility(View.VISIBLE);

        mAdapter = new ArtistsListAdapter(getActivity(), new ArrayList<Artist>());
        mStickyList.setAdapter(mAdapter);
    }

    public void onEventMainThread(ArtistDeletedEvent event){
        jobManager.addJobInBackground(new FetchArtistsJob(getActivity()));
    }
}
