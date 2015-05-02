package com.qwertyfinger.musicreleasestracker.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

    private SwipeRefreshLayout swipeContainer;
    private JobManager jobManager;
    private ReleasesListAdapter mAdapter;
    private StickyListHeadersListView mStickyList;

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
        jobManager.addJobInBackground(new FetchReleasesJob(getActivity()));

        mStickyList = (StickyListHeadersListView) view.findViewById(R.id.list);
        mStickyList.setOnScrollListener(new ListScrollListener(getActivity()));
        mStickyList.setAreHeadersSticky(false);

        return view;
    }

    public void onEventMainThread(ReleasesFetchedEvent event) {
        mAdapter = new ReleasesListAdapter(getActivity(), event.getReleases());
        mStickyList.setAdapter(mAdapter);
    }

    public void onEventMainThread(ReleasesChangedEvent event) {
        jobManager.addJobInBackground(new FetchReleasesJob(getActivity()));
        DatabaseHandler db = DatabaseHandler.getInstance(getActivity());

        Log.d("Reading: ", "Reading all releases..");
        Log.d("Count: ", ""+db.getReleasesCount());
        List<Release> list = db.getAllReleases();
        for (Release release : list) {
            String log = "Id: " + release.getId() + " ,Title: " + release.getTitle() + " ,Artist: " + release.getArtist()
                    + " ,Date: " + release.getDate() + " ,ImageUrl: " + release.getImageUri();
            Log.d("Name: ", log);
        }
    }

    public void onEventMainThread(NoArtistsEvent event){
        mAdapter = new ReleasesListAdapter(getActivity(), new ArrayList<Release>());
        mStickyList.setAdapter(mAdapter);

        CharSequence text = "You have no subscriptions";
        final Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, context.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material));
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);
    }

    public void onEventMainThread(NoReleasesEvent event){
        mAdapter = new ReleasesListAdapter(getActivity(), new ArrayList<Release>());
        mStickyList.setAdapter(mAdapter);

        CharSequence text = "No releases";
        final Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, context.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material));
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);
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

}
