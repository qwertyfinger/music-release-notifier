package com.qwertyfinger.musicreleasestracker.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.qwertyfinger.musicreleasestracker.events.ArtistExistsEvent;
import com.qwertyfinger.musicreleasestracker.events.SearchQueryEvent;
import com.qwertyfinger.musicreleasestracker.events.SearchingEvent;
import com.qwertyfinger.musicreleasestracker.jobs.RefreshReleasesJob;
import com.qwertyfinger.musicreleasestracker.jobs.SearchArtistJob;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.qwertyfinger.musicreleasestracker.misc.ListScrollListener;

import java.util.List;

import de.greenrobot.event.EventBus;

public class AddSubscriptions extends ActionBarActivity{

    private boolean artistAdded = false;
    private JobManager jobManager;
    private ProgressBar spinner;
    private TextView noResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subscriptions);

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        noResult = (TextView) findViewById(R.id.noresult);
        noResult.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        jobManager = App.getInstance().getJobManager();
        handleIntent(getIntent());
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
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            if (query.trim().length() > 1)
                jobManager.addJobInBackground(new SearchArtistJob(query));
        }
    }

    public void onEventMainThread(SearchingEvent event){
        TextView empty = (TextView) findViewById(R.id.empty);

        if (empty.getVisibility() != View.GONE)
            empty.setVisibility(View.GONE);

        if (noResult.getVisibility() != View.GONE)
            noResult.setVisibility(View.GONE);

        if (spinner.getVisibility() == View.GONE)
            spinner.setVisibility(View.VISIBLE);
    }

    public void onEventMainThread(SearchQueryEvent event){
        if (spinner.getVisibility() != View.GONE)
            spinner.setVisibility(View.GONE);

        if (noResult.getVisibility() == View.GONE)
            noResult.setVisibility(View.VISIBLE);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setOnScrollListener(new ListScrollListener(this));
        listView.setEmptyView(noResult);
        listView.setAdapter(new SearchResultsAdapter(this, event.getSearchResults()));
    }

    public void onEventMainThread(ArtistAddedEvent event) {
        artistAdded = true;

        DatabaseHandler db = DatabaseHandler.getInstance(this);

        Log.d("Reading: ", "Reading all contacts..");
        List<Artist> list = db.getAllArtists();
        for (Artist artist : list) {
            String log = "Id: " + artist.getId() + " ,Name: " + artist.getTitle() + " ,ImageUrl: " + artist.getImageUri();
            Log.d("Name: ", log);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_subscriptions, menu);
        MenuItem search = menu.findItem(R.id.subscriptions_search);
        // Get the SearchView and set the searchable configuration

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//        MenuItemCompat.expandActionView(search);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (artistAdded == true)
            jobManager.addJobInBackground(new RefreshReleasesJob(this, Constants.AFTER_ADDING_REFRESH));
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        }
        catch (Throwable t){
            //in case registration didn't go through
        }
    }
}
