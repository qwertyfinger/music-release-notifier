package com.qwertyfinger.musicreleasetracker.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.adapters.ArtistsListAdapter;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.events.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasetracker.events.ArtistsFetchedEvent;
import com.qwertyfinger.musicreleasetracker.events.NoArtistsEvent;
import com.qwertyfinger.musicreleasetracker.jobs.EmptyArtistsJob;
import com.qwertyfinger.musicreleasetracker.jobs.FetchArtistsJob;
import com.qwertyfinger.musicreleasetracker.misc.ListScrollListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ArtistsFragment extends Fragment {

    private JobManager jobManager;
    private ArtistsListAdapter mAdapter;
    private StickyListHeadersListView mStickyList;
    private TextView mNoArtists;
    private Parcelable state;
    private List<Artist> fetchedArtists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
        jobManager = App.getInstance().getJobManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_artists, container, false);

        if (savedInstanceState != null){

            mNoArtists = (TextView) view.findViewById(R.id.noArtists);
            if (savedInstanceState.getInt("noArtistVisibility") == View.VISIBLE)
                mNoArtists.setVisibility(View.VISIBLE);
            else
                mNoArtists.setVisibility(View.GONE);

            mStickyList = (StickyListHeadersListView) view.findViewById(R.id.artistsList);
            mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
            mStickyList.setAreHeadersSticky(false);

            int fetchListSize = savedInstanceState.getInt("fetchedListSize");
            if (fetchListSize != -1) {
                if (fetchListSize > 0) {
                    List<String> ids = savedInstanceState.getStringArrayList("ids");
                    List<String> titles = savedInstanceState.getStringArrayList("titles");
                    List<String> images = savedInstanceState.getStringArrayList("images");

                    if (fetchedArtists == null)
                        fetchedArtists = new ArrayList<>();

                    for (int i = 0; i < savedInstanceState.getInt("fetchedListSize"); i++) {
                        fetchedArtists.add(i, new Artist(ids.get(i), titles.get(i), images.get(i)));
                    }
                    mAdapter = new ArtistsListAdapter(getActivity(), fetchedArtists);
                    mStickyList.setAdapter(mAdapter);
                }
            }
        }

        else {
            jobManager.addJobInBackground(new FetchArtistsJob(getActivity()));

            mNoArtists = (TextView) view.findViewById(R.id.noArtists);

            mStickyList = (StickyListHeadersListView) view.findViewById(R.id.artistsList);
            mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
            mStickyList.setAreHeadersSticky(false);
        }

        if (state != null)
            mStickyList.onRestoreInstanceState(state);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.artists_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty_artists:
                jobManager.addJobInBackground(new EmptyArtistsJob(getActivity()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mStickyList != null)
            state = mStickyList.onSaveInstanceState();

        if (mNoArtists != null)
            savedInstanceState.putInt("noArtistVisibility", mNoArtists.getVisibility());

        if (fetchedArtists != null) {
            savedInstanceState.putInt("fetchedListSize", fetchedArtists.size());

            if (fetchedArtists.size() > 0) {
                ArrayList<String> ids = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();

                for (int i = 0; i < fetchedArtists.size(); i++) {
                    ids.add(i, fetchedArtists.get(i).getId());
                    titles.add(i, fetchedArtists.get(i).getTitle());
                    images.add(i, fetchedArtists.get(i).getImage());
                }

                savedInstanceState.putStringArrayList("ids", ids);
                savedInstanceState.putStringArrayList("titles", titles);
                savedInstanceState.putStringArrayList("images", images);
            }
        }
        else
            savedInstanceState.putInt("fetchedListSize", -1);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onEventMainThread(ArtistsFetchedEvent event){
        mNoArtists.setVisibility(View.GONE);

        fetchedArtists = event.getArtists();

        mAdapter = new ArtistsListAdapter(getActivity(), fetchedArtists);
        mStickyList.setAdapter(mAdapter);
    }

    public void onEventMainThread(NoArtistsEvent event){
        mNoArtists.setVisibility(View.VISIBLE);

        fetchedArtists = null;

        mAdapter = new ArtistsListAdapter(getActivity(), new ArrayList<Artist>());
        mStickyList.setAdapter(mAdapter);
    }

    public void onEventMainThread(ArtistDeletedEvent event){
        if (fetchedArtists != null) {
            if (fetchedArtists.contains(event.getArtist()))
                fetchedArtists.remove(event.getArtist());
        }

        if (mAdapter != null) {
            mAdapter.remove(event.getArtist());
            mAdapter.notifyDataSetChanged();
        }
    }
}
