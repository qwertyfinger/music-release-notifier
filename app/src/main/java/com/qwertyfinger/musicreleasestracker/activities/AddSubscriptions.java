package com.qwertyfinger.musicreleasestracker.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class AddSubscriptions extends AppCompatActivity{

    private List<Artist> addedArtists = null;
    private JobManager jobManager;
    private ProgressBar spinner;
    private TextView noResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subscriptions);

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        noResult = (TextView) findViewById(R.id.noResult);
        noResult.setVisibility(View.GONE);

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
        getMenuInflater().inflate(R.menu.menu_add_subscriptions, menu);
        MenuItem search = menu.findItem(R.id.subscriptions_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);

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
                    jobManager.addJobInBackground(new RefreshReleasesJob(activity, Constants.AFTER_ADDING_REFRESH, addedArtists));
                    jobManager.addJobInBackground(new FetchArtistsJob(activity));
                }
                return false;
            }
        });

        MenuItemCompat.expandActionView(search);
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

    public void onEventMainThread(SearchingEvent event){
        TextView empty = (TextView) findViewById(R.id.empty);

        empty.setVisibility(View.GONE);

        spinner.setVisibility(View.VISIBLE);
    }

    public void onEventMainThread(SearchQueryEvent event){
        spinner.setVisibility(View.GONE);

        ListView listView = (ListView) findViewById(R.id.searchList);
        listView.setOnScrollListener(new ListScrollListener(this));
        listView.setEmptyView((TextView) findViewById(R.id.noResult));
        listView.setAdapter(new SearchResultsAdapter(this, event.getSearchResults()));
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
