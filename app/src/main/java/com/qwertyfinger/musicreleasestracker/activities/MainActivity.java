package com.qwertyfinger.musicreleasestracker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasestracker.App;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.adapters.MyFragmentPagerAdapter;
import com.qwertyfinger.musicreleasestracker.jobs.EmptyArtistsJob;
import com.qwertyfinger.musicreleasestracker.jobs.EmptyReleasesJob;
import com.qwertyfinger.musicreleasestracker.jobs.RefreshReleasesJob;


public class MainActivity extends ActionBarActivity {

    JobManager jobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jobManager = App.getInstance().getJobManager();

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
        tabsStrip.setBackgroundColor(Color.parseColor("#228B22"));

        // Attach the page change listener to tab strip and **not** the view pager inside the activity
        /*tabsStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_add_subscription:
                startActivity(new Intent(this, AddSubscriptions.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_refresh:
                jobManager.addJobInBackground(new RefreshReleasesJob(this, Constants.EXPLICIT_REFRESH));
                return true;
            case R.id.action_empty_artists:
                jobManager.addJobInBackground(new EmptyArtistsJob(this));
                return true;
            case R.id.action_empty_releases:
                jobManager.addJobInBackground(new EmptyReleasesJob(this));
                return true;

            default: return super.onOptionsItemSelected(item);
        }
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
    public void onDestroy(){
        super.onDestroy();
    }

}
