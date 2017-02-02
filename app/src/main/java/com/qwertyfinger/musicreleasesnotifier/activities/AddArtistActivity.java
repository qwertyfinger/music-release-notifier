package com.qwertyfinger.musicreleasesnotifier.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.adapters.SearchResultsAdapter;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.ReleaseAdapterEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistAddedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistExistsEvent;
import com.qwertyfinger.musicreleasesnotifier.events.search.SearchQueryEvent;
import com.qwertyfinger.musicreleasesnotifier.events.search.SearchingEvent;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.FetchArtistsJob;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.SearchArtistJob;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.RefreshReleasesJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.ListScrollListener;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class AddArtistActivity extends AppCompatActivity{

    private List<Artist> addedArtists = null;
    private List<Artist> deletedArtists = null;

    private JobManager jobManager;
    private ProgressBar spinner;
    private TextView noResult;
    private TextView empty;
    private SearchView searchView;
    private ListView listView;
    private Parcelable state;
    private List<Artist> searchResults;
    private CharSequence searchQuery = "";
    private boolean searchFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_artists);

        if (savedInstanceState != null) {
            empty = (TextView) findViewById(R.id.empty);
            if (savedInstanceState.getInt("emptyVisibility") == View.VISIBLE)
                empty.setVisibility(View.VISIBLE);
            else
                empty.setVisibility(View.GONE);

            spinner = (ProgressBar) findViewById(R.id.progressBar);
            if (savedInstanceState.getInt("spinnerVisibility") == View.VISIBLE)
                    spinner.setVisibility(View.VISIBLE);
            else
                    spinner.setVisibility(View.GONE);

            noResult = (TextView) findViewById(R.id.noResult);
            if (savedInstanceState.getInt("noResultVisibility") == View.VISIBLE)
                    noResult.setVisibility(View.VISIBLE);
            else
                    noResult.setVisibility(View.GONE);

            listView = (ListView) findViewById(R.id.searchList);
            listView.setOnScrollListener(new ListScrollListener(this));

            searchQuery = savedInstanceState.getCharSequence("searchQuery");
            searchFocus = savedInstanceState.getBoolean("searchFocus");

            addedArtists = savedInstanceState.getParcelableArrayList("addedList");

            deletedArtists = savedInstanceState.getParcelableArrayList("deletedArtist");

            searchResults = savedInstanceState.getParcelableArrayList("searchList");
            if (searchResults == null)
                searchResults = new ArrayList<>();
            if (searchResults.size() > 0) {
                listView.setEmptyView((TextView) findViewById(R.id.noResult));
                listView.setAdapter(new SearchResultsAdapter(this, searchResults));
            }
        }

        else {
            searchFocus = true;

            empty = (TextView) findViewById(R.id.empty);

            spinner = (ProgressBar)findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);

            noResult = (TextView) findViewById(R.id.noResult);
            noResult.setVisibility(View.GONE);
        }

        if(state != null)
            listView.onRestoreInstanceState(state);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        jobManager = App.getInstance().getJobManager();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            jobManager.addJobInBackground(new SearchArtistJob(query));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_add_artists, menu);
        MenuItem search = menu.findItem(R.id.subscriptions_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search artists...");

        final Activity activity = this;
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            private boolean extended = false;

            @Override
            public void onClick(View v) {
                if (!extended) {
                    extended = true;
                    ViewGroup.LayoutParams lp = v.getLayoutParams();
                    lp.width = ActionBar.LayoutParams.MATCH_PARENT;
                }
            }
        });

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                NavUtils.navigateUpFromSameTask(activity);

                if (addedArtists != null) {
                    if (!addedArtists.isEmpty()) {
                        jobManager.addJobInBackground(new FetchArtistsJob(activity));
                        jobManager.addJobInBackground(new RefreshReleasesJob(activity, Constants.AFTER_ADDING_REFRESH, addedArtists));
                    }
                    else {
                        if (deletedArtists != null) {
                            if (!deletedArtists.isEmpty())
                                jobManager.addJobInBackground(new FetchArtistsJob(activity));
                        }
                    }
                }
                else {
                    if (deletedArtists != null) {
                        if (!deletedArtists.isEmpty())
                            jobManager.addJobInBackground(new FetchArtistsJob(activity));
                    }
                }
                return false;
            }
        });

        MenuItemCompat.expandActionView(search);
        searchView.setQuery(searchQuery, false);
        if (!searchFocus)
            searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().post(new ReleaseAdapterEvent());
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (listView != null)
            state = listView.onSaveInstanceState();

        if (spinner != null)
            savedInstanceState.putInt("spinnerVisibility", spinner.getVisibility());

        if (empty != null)
            savedInstanceState.putInt("emptyVisibility", empty.getVisibility());

        if (noResult != null)
            savedInstanceState.putInt("noResultVisibility", noResult.getVisibility());

        if (searchView != null) {
            savedInstanceState.putCharSequence("searchQuery", searchView.getQuery());
            savedInstanceState.putBoolean("searchFocus", searchView.hasFocus());
        }
        else
            savedInstanceState.putCharSequence("searchQuery", "");

        if (addedArtists != null)
            savedInstanceState.putParcelableArrayList("addedList", (ArrayList<Artist>) addedArtists);

        if (deletedArtists != null)
            savedInstanceState.putParcelableArrayList("deletedList", (ArrayList<Artist>) deletedArtists);

        if (searchResults != null)
            savedInstanceState.putParcelableArrayList("searchList", (ArrayList<Artist>) searchResults);

        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SearchingEvent event){
        empty.setVisibility(View.GONE);
        noResult.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SearchQueryEvent event) {
        spinner.setVisibility(View.GONE);

        searchResults = event.getSearchResults();

        listView = (ListView) findViewById(R.id.searchList);
        listView.setOnScrollListener(new ListScrollListener(this));
        listView.setEmptyView((TextView) findViewById(R.id.noResult));
        listView.setAdapter(new SearchResultsAdapter(this, searchResults));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ArtistAddedEvent event) {
        if (addedArtists == null)
            addedArtists = new ArrayList<>();
        addedArtists.add(event.getArtist());

        if (deletedArtists != null)
            deletedArtists.remove(event.getArtist());

        CharSequence text = "Added " + event.getArtist().getTitle();
        Utils.makeToast(this, Toast.LENGTH_SHORT, 1000, text);

        DatabaseHandler db = DatabaseHandler.getInstance(this);

        Log.d("Reading: ", "Reading all artists..");
        Log.d("Count: ", db.getArtistsCount() + "");
        List<Artist> list = db.getAllArtists();
        for (Artist artist : list) {
            String log = "Id: " + artist.getId() + " ,Name: " + artist.getTitle() + " ,ImageUrl: " + artist.getImage();
            Log.d("Artist: ", log);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ArtistDeletedEvent event) {
        if (addedArtists != null)
            addedArtists.remove(event.getArtist());

        if (deletedArtists == null)
            deletedArtists = new ArrayList<>();
        deletedArtists.add(event.getArtist());

        CharSequence text = "Removed " + event.getArtist().getTitle();
        Utils.makeToast(this, Toast.LENGTH_SHORT, 1000, text);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ArtistExistsEvent event){
        CharSequence text = "You are already subscribed to this artist";
        Utils.makeToast(this, Toast.LENGTH_SHORT, 1000, text);
    }
}
