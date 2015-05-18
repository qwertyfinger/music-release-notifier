package com.qwertyfinger.musicreleasetracker.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.adapters.ReleasesListAdapter;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.entities.Release;
import com.qwertyfinger.musicreleasetracker.events.artist.ArtistsChangedEvent;
import com.qwertyfinger.musicreleasetracker.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasetracker.events.release.NoReleasesEvent;
import com.qwertyfinger.musicreleasetracker.events.release.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasetracker.events.release.ReleasesFetchedEvent;
import com.qwertyfinger.musicreleasetracker.jobs.release.FetchReleasesJob;
import com.qwertyfinger.musicreleasetracker.misc.ListScrollListener;

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
    private Parcelable state;
    private List<Release> fetchedReleases;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        jobManager = App.getInstance().getJobManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_releases, container, false);

        mNoArtists = (TextView) view.findViewById(R.id.noArtistsInRel);
        mNoReleases = (TextView) view.findViewById(R.id.noReleases);

        mStickyList = (StickyListHeadersListView) view.findViewById(R.id.releasesList);
        mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
        mStickyList.setAreHeadersSticky(false);

        mNoReleases = (TextView) view.findViewById(R.id.noReleases);

        mNoArtists = (TextView) view.findViewById(R.id.noArtistsInRel);

        if (savedInstanceState != null){

            if (savedInstanceState.getInt("noArtistVisibility") == View.VISIBLE)
                mNoArtists.setVisibility(View.VISIBLE);
            else
                mNoArtists.setVisibility(View.GONE);

            if (savedInstanceState.getInt("noReleasesVisibility") == View.VISIBLE)
                mNoReleases.setVisibility(View.VISIBLE);
            else
                mNoReleases.setVisibility(View.GONE);

            fetchedReleases = savedInstanceState.getParcelableArrayList("fetchedList");
            if (fetchedReleases == null)
                fetchedReleases = new ArrayList<>();
            if (fetchedReleases.size() > 0) {
                mAdapter = new ReleasesListAdapter(getActivity(), fetchedReleases);
                mStickyList.setAdapter(mAdapter);
            }
        }

        else {
            fetchedReleases = new ArrayList<>();
            mNoReleases.setVisibility(View.GONE);
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

        if (mNoReleases != null)
            savedInstanceState.putInt("noReleasesVisibility", mNoReleases.getVisibility());

        if (mNoArtists != null)
            savedInstanceState.putInt("noArtistVisibility", mNoArtists.getVisibility());

        if (fetchedReleases != null)
            savedInstanceState.putParcelableArrayList("fetchedList", (ArrayList<Release>) fetchedReleases);

        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ArtistsChangedEvent event) {
        mNoArtists.setVisibility(View.GONE);
        if (DatabaseHandler.getInstance(getActivity()).getReleasesCount() == 0)
            mNoReleases.setVisibility(View.VISIBLE);
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
