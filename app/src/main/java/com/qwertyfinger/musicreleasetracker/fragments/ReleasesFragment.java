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

        if (savedInstanceState != null){

            mNoArtists = (TextView) view.findViewById(R.id.noArtistsInRel);
            if (savedInstanceState.getInt("noArtistVisibility") == View.VISIBLE)
                mNoArtists.setVisibility(View.VISIBLE);
            else
                mNoArtists.setVisibility(View.GONE);

            mNoReleases = (TextView) view.findViewById(R.id.noReleases);
            if (savedInstanceState.getInt("noReleasesVisibility") == View.VISIBLE)
                mNoReleases.setVisibility(View.VISIBLE);
            else
                mNoReleases.setVisibility(View.GONE);

            mStickyList = (StickyListHeadersListView) view.findViewById(R.id.releasesList);
            mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
            mStickyList.setAreHeadersSticky(false);

            int fetchedListSize = savedInstanceState.getInt("fetchedListSize");
            if (fetchedListSize != -1) {
                if (fetchedListSize > 0) {
                    ArrayList<String> ids = savedInstanceState.getStringArrayList("ids");
                    ArrayList<String> titles = savedInstanceState.getStringArrayList("titles");
                    ArrayList<String> images = savedInstanceState.getStringArrayList("images");
                    ArrayList<String> artists = savedInstanceState.getStringArrayList("artists");
                    ArrayList<String> dates = savedInstanceState.getStringArrayList("dates");

                    if (fetchedReleases == null)
                        fetchedReleases = new ArrayList<>();

                    for (int i = 0; i < savedInstanceState.getInt("fetchedListSize"); i++) {
                        fetchedReleases.add(i, new Release(ids.get(i), titles.get(i), artists.get(i), dates.get(i), images.get(i)));
                    }

                    mAdapter = new ReleasesListAdapter(getActivity(), fetchedReleases);
                    mStickyList.setAdapter(mAdapter);
                }
            }
        }

        else {
            if (App.firstLoad) {
                jobManager.addJobInBackground(new FetchReleasesJob(getActivity()));
            }

            mNoArtists = (TextView) view.findViewById(R.id.noArtistsInRel);
            mNoReleases = (TextView) view.findViewById(R.id.noReleases);
            mNoArtists.setVisibility(View.GONE);

            mStickyList = (StickyListHeadersListView) view.findViewById(R.id.releasesList);
            mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
            mStickyList.setAreHeadersSticky(false);
        }

        if (state != null)
            mStickyList.onRestoreInstanceState(state);

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

        if (fetchedReleases != null) {
            savedInstanceState.putInt("fetchedListSize", fetchedReleases.size());

            if (fetchedReleases.size() > 0) {
                ArrayList<String> ids = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();
                ArrayList<String> artists = new ArrayList<>();
                ArrayList<String> dates = new ArrayList<>();

                for (int i = 0; i < fetchedReleases.size(); i++) {
                    ids.add(i, fetchedReleases.get(i).getId());
                    titles.add(i, fetchedReleases.get(i).getTitle());
                    images.add(i, fetchedReleases.get(i).getImage());
                    artists.add(i, fetchedReleases.get(i).getArtist());
                    dates.add(i, fetchedReleases.get(i).getDate());
                }

                savedInstanceState.putStringArrayList("ids", ids);
                savedInstanceState.putStringArrayList("titles", titles);
                savedInstanceState.putStringArrayList("images", images);
                savedInstanceState.putStringArrayList("artists", artists);
                savedInstanceState.putStringArrayList("dates", dates);
            }
        }
        else
            savedInstanceState.putInt("fetchedListSize", -1);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onEventMainThread(ArtistsChangedEvent event) {
        mNoArtists.setVisibility(View.GONE);
        if (DatabaseHandler.getInstance(getActivity()).getReleasesCount() == 0)
            mNoReleases.setVisibility(View.VISIBLE);
    }

    public void onEventMainThread(ReleasesFetchedEvent event) {
        mNoReleases.setVisibility(View.GONE);
        mNoArtists.setVisibility(View.GONE);

        fetchedReleases = event.getReleases();

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

        fetchedReleases = null;

        mAdapter = new ReleasesListAdapter(getActivity(), new ArrayList<Release>());
        mStickyList.setAdapter(mAdapter);
    }

}
