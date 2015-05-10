package com.qwertyfinger.musicreleasestracker.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.SearchRecentSuggestions;
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
import com.qwertyfinger.musicreleasestracker.App;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.adapters.SearchResultsAdapter;
import com.qwertyfinger.musicreleasestracker.contentProviders.MySuggestionProvider;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ArtistAddedEvent;
import com.qwertyfinger.musicreleasestracker.events.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasestracker.events.ArtistExistsEvent;
import com.qwertyfinger.musicreleasestracker.events.ReleaseAdapterEvent;
import com.qwertyfinger.musicreleasestracker.events.SearchQueryEvent;
import com.qwertyfinger.musicreleasestracker.events.SearchingEvent;
import com.qwertyfinger.musicreleasestracker.jobs.FetchArtistsJob;
import com.qwertyfinger.musicreleasestracker.jobs.RefreshReleasesJob;
import com.qwertyfinger.musicreleasestracker.jobs.SearchArtistJob;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.qwertyfinger.musicreleasestracker.misc.ListScrollListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class AddArtistActivity extends AppCompatActivity{

    private List<Artist> addedArtists = null;
    private JobManager jobManager;
    private ProgressBar spinner;
    private TextView noResult;
    private TextView empty;
    private SearchView searchView;
    private ListView listView;
    private Parcelable state;
    private List<Artist> searchResults;
    private CharSequence searchQuery = "";

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
            listView.setEmptyView((TextView) findViewById(R.id.noResult));

            searchQuery = savedInstanceState.getCharSequence("searchQuery");

            int addedListSize = savedInstanceState.getInt("addedListSize");
            if (addedListSize != -1) {
                if (addedListSize > 0) {
                    List<String> ids = savedInstanceState.getStringArrayList("ids");
                    List<String> titles = savedInstanceState.getStringArrayList("titles");
                    List<String> images = savedInstanceState.getStringArrayList("images");

                    if (addedArtists == null)
                        addedArtists = new ArrayList<>();

                    for (int i = 0; i < savedInstanceState.getInt("addedListSize"); i++) {
                        addedArtists.add(i, new Artist(ids.get(i), titles.get(i), images.get(i)));
                    }
                }
            }

            int searchListSize = savedInstanceState.getInt("searchListSize");
            if (searchListSize != -1) {
                if (searchListSize > 0) {
                    List<String> ids = savedInstanceState.getStringArrayList("ids2");
                    List<String> titles = savedInstanceState.getStringArrayList("titles2");
                    List<String> images = savedInstanceState.getStringArrayList("images2");

                    if (searchResults == null)
                        searchResults = new ArrayList<>();

                    for (int i = 0; i < savedInstanceState.getInt("searchListSize"); i++) {
                        searchResults.add(i, new Artist(ids.get(i), titles.get(i), images.get(i)));
                    }
                    listView.setAdapter(new SearchResultsAdapter(this, searchResults));
                }
            }
        }

        else {
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
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

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
                }
                return false;
            }
        });

        MenuItemCompat.expandActionView(search);
        searchView.setQuery(searchQuery, false);
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
    protected void onResume(){
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

        if (searchView != null)
            savedInstanceState.putCharSequence("searchQuery", searchView.getQuery());
        else
            savedInstanceState.putCharSequence("searchQuery", "");

        if (addedArtists != null) {
            savedInstanceState.putInt("addedListSize", addedArtists.size());

            if (addedArtists.size() > 0) {
                ArrayList<String> ids = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();

                for (int i = 0; i < addedArtists.size(); i++) {
                    ids.add(i, addedArtists.get(i).getId());
                    titles.add(i, addedArtists.get(i).getTitle());
                    images.add(i, addedArtists.get(i).getImage());
                }

                savedInstanceState.putStringArrayList("ids", ids);
                savedInstanceState.putStringArrayList("titles", titles);
                savedInstanceState.putStringArrayList("images", images);
            }
        }
        else
            savedInstanceState.putInt("addedListSize", -1);

        if (searchResults != null) {
            savedInstanceState.putInt("searchListSize", searchResults.size());

            if (searchResults.size() > 0) {
                ArrayList<String> ids = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> images = new ArrayList<>();

                for (int i = 0; i < searchResults.size(); i++) {
                    ids.add(i, searchResults.get(i).getId());
                    titles.add(i, searchResults.get(i).getTitle());
                    images.add(i, searchResults.get(i).getImage());
                }

                savedInstanceState.putStringArrayList("ids2", ids);
                savedInstanceState.putStringArrayList("titles2", titles);
                savedInstanceState.putStringArrayList("images2", images);
            }
        }
        else
            savedInstanceState.putInt("searchListSize", -1);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onEventMainThread(SearchingEvent event){
        empty.setVisibility(View.GONE);

        spinner.setVisibility(View.VISIBLE);
    }

    public void onEventMainThread(SearchQueryEvent event) {
        spinner.setVisibility(View.GONE);

        searchResults = event.getSearchResults();

        listView = (ListView) findViewById(R.id.searchList);
        listView.setOnScrollListener(new ListScrollListener(this));
        listView.setEmptyView((TextView) findViewById(R.id.noResult));
        listView.setAdapter(new SearchResultsAdapter(this, searchResults));
    }

    public void onEventMainThread(ArtistAddedEvent event) {
        if (addedArtists == null)
            addedArtists = new ArrayList<>();
        addedArtists.add(event.getArtist());

        CharSequence text = "Added " + event.getArtist().getTitle();
        final Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, context.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material));
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);

        DatabaseHandler db = DatabaseHandler.getInstance(this);

        Log.d("Reading: ", "Reading all artists..");
        Log.d("Count: ", db.getArtistsCount() + "");
        List<Artist> list = db.getAllArtists();
        for (Artist artist : list) {
            String log = "Id: " + artist.getId() + " ,Name: " + artist.getTitle() + " ,ImageUrl: " + artist.getImage();
            Log.d("Artist: ", log);
        }
    }

    public void onEventMainThread(ArtistDeletedEvent event) {
        if (addedArtists != null)
            addedArtists.remove(event.getArtist());

        CharSequence text = "Removed " + event.getArtist().getTitle();
        final Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
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

    public void onEventMainThread(ArtistExistsEvent event){
        CharSequence text = "You are already subscribed to this artist";
        final Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
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
}