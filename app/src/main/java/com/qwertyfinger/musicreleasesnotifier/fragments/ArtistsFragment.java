package com.qwertyfinger.musicreleasesnotifier.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.qwertyfinger.musicreleasesnotifier.adapters.ArtistsListAdapter;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistsChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistsFetchedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.FetchArtistsJob;
import com.qwertyfinger.musicreleasesnotifier.misc.ListScrollListener;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

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

        mNoArtists = (TextView) view.findViewById(R.id.noArtists);

        mStickyList = (StickyListHeadersListView) view.findViewById(R.id.artistsList);
        mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
        mStickyList.setAreHeadersSticky(false);

        if (savedInstanceState != null){

            if (savedInstanceState.getInt("noArtistVisibility") == View.VISIBLE)
                mNoArtists.setVisibility(View.VISIBLE);
            else
                mNoArtists.setVisibility(View.GONE);

            fetchedArtists = savedInstanceState.getParcelableArrayList("fetchedList");
            if (fetchedArtists == null)
                fetchedArtists = new ArrayList<>();
            if (fetchedArtists.size() > 0) {
                mAdapter = new ArtistsListAdapter(getActivity(), fetchedArtists);
                mStickyList.setAdapter(mAdapter);
            }
        }

        else {
            fetchedArtists = new ArrayList<>();
            if (!Utils.isExternalStorageWritable()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.storage_warning_title);
                builder.setMessage(R.string.storage_warning_message);
                builder.setNeutralButton(R.string.storage_warning_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }

        if (state != null)
            mStickyList.onRestoreInstanceState(state);

        if (DatabaseHandler.getInstance(getActivity()).getArtistsCount() != fetchedArtists.size())
            jobManager.addJobInBackground(new FetchArtistsJob(getActivity()));

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
        if (mNoArtists != null)
            savedInstanceState.putInt("noArtistVisibility", mNoArtists.getVisibility());

        if (fetchedArtists != null)
            savedInstanceState.putParcelableArrayList("fetchedList", (ArrayList<Artist>) fetchedArtists);

        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressWarnings("unused")
    public void onEvent(ArtistsChangedEvent event) {
        if (event.getArtists() != null)
            jobManager.addJobInBackground(new FetchArtistsJob(getActivity()));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ArtistsFetchedEvent event){
        mNoArtists.setVisibility(View.GONE);

        fetchedArtists = event.getArtists();

        mAdapter = new ArtistsListAdapter(getActivity(), fetchedArtists);
        mStickyList.setAdapter(mAdapter);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NoArtistsEvent event){
        mNoArtists.setVisibility(View.VISIBLE);

        fetchedArtists = null;

        mAdapter = new ArtistsListAdapter(getActivity(), new ArrayList<Artist>());
        mStickyList.setAdapter(mAdapter);
    }

    @SuppressWarnings("unused")
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
