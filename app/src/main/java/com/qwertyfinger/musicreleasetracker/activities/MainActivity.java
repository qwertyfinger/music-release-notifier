package com.qwertyfinger.musicreleasetracker.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.Utils;
import com.qwertyfinger.musicreleasetracker.adapters.MainFragmentPagerAdapter;
import com.qwertyfinger.musicreleasetracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasetracker.events.artist.NoArtistsEvent;
import com.qwertyfinger.musicreleasetracker.events.deezer.LoggedInDeezerEvent;
import com.qwertyfinger.musicreleasetracker.events.deezer.LoggedOutDeezerEvent;
import com.qwertyfinger.musicreleasetracker.events.lastfm.LoggedInLastfmEvent;
import com.qwertyfinger.musicreleasetracker.events.lastfm.LoggedOutLastfmEvent;
import com.qwertyfinger.musicreleasetracker.events.sync.SyncFinishedEvent;
import com.qwertyfinger.musicreleasetracker.events.sync.SyncInProgressEvent;
import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasetracker.jobs.artist.EmptyArtistsJob;
import com.qwertyfinger.musicreleasetracker.jobs.release.RefreshReleasesJob;
import com.qwertyfinger.musicreleasetracker.jobs.sync.SyncDeezerJob;
import com.qwertyfinger.musicreleasetracker.jobs.sync.SyncLastfmJob;
import com.qwertyfinger.musicreleasetracker.receivers.NewDayReceiver;

import java.util.Calendar;

import de.greenrobot.event.EventBus;


public class MainActivity extends AppCompatActivity {

    JobManager jobManager;
    private RelativeLayout syncBar;

    private MenuItem emptyArtist;
    private MenuItem deezerSync;
    private MenuItem lastfmSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        App.firstLoad = false;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_action_mrt);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

//       encryption key generation, unused for now
        /*File file = new File(getFilesDir(), "spaceOdyssey");
        if (!file.exists()) {
            try {
                Utils.generateKey(this);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }*/

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        jobManager = App.getInstance().getJobManager();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MainFragmentPagerAdapter(getSupportFragmentManager()));

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);
        tabsStrip.setBackgroundColor(Color.parseColor("#228B22"));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        if (!settings.getBoolean(SettingsFragment.NEWDAY_ALARM_SET, false)) {
            AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent receiverIntent = new Intent(this, NewDayReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, receiverIntent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 24);

            alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
            editor.putBoolean(SettingsFragment.NEWDAY_ALARM_SET, true);
        }

        if (settings.getBoolean(SettingsFragment.RELEASE_REFRESH_ALARM_SET, false)) {
            int frequency = Integer.parseInt(settings.getString(SettingsFragment.SYNC_FREQUENCY, "5"));

            AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent receiverIntent = new Intent(this, NewDayReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, receiverIntent, 0);

            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 1000 - Utils.generateRandom()
                            * 1000, alarmIntent);

            editor.putBoolean(SettingsFragment.RELEASE_REFRESH_ALARM_SET, true);
        }

        syncBar = (RelativeLayout) findViewById(R.id.syncBar);

        if (settings.getBoolean(SettingsFragment.SYNC_IN_PROGRESS, false))
                syncBar.setVisibility(View.VISIBLE);
        else
                syncBar.setVisibility(View.GONE);


        editor.apply();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        deezerSync = menu.findItem(R.id.action_deezer_sync);
        lastfmSync = menu.findItem(R.id.action_lastfm_sync);
        emptyArtist = menu.findItem(R.id.action_empty_artists);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.DEEZER, false))
            deezerSync.setVisible(true);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.LAST_FM_FLAG, false))
            lastfmSync.setVisible(true);
        if (DatabaseHandler.getInstance(this).getArtistsCount() != 0)
            emptyArtist.setVisible(true);

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.firstLoad = true;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_subscription:
                startActivity(new Intent(this, AddArtistActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_refresh:
                jobManager.addJobInBackground(new RefreshReleasesJob(this, Constants.EXPLICIT_REFRESH));
                return true;
            case R.id.action_empty_artists:
                if (!Utils.isSyncInProgress(this))
                    jobManager.addJobInBackground(new EmptyArtistsJob(this));
                else
                    Utils.makeSyncToast(this);
                return true;
            case R.id.action_deezer_sync:
                if (!Utils.isSyncInProgress(this))
                    jobManager.addJobInBackground(new SyncDeezerJob(this, Constants.EXPLICIT_SYNC));
                else
                    Utils.makeSyncToast(this);
                return true;
            case R.id.action_lastfm_sync:
                if (!Utils.isSyncInProgress(this))
                    jobManager.addJobInBackground(new SyncLastfmJob(this, Constants.EXPLICIT_SYNC));
                else
                    Utils.makeSyncToast(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncInProgressEvent event) {
        syncBar.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncFinishedEvent event) {
        syncBar.setVisibility(View.GONE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoggedInLastfmEvent event) {
        if (lastfmSync != null)
            lastfmSync.setVisible(true);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoggedOutLastfmEvent event) {
        if (lastfmSync != null)
            lastfmSync.setVisible(false);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoggedInDeezerEvent event) {
        if (deezerSync != null)
            deezerSync.setVisible(true);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoggedOutDeezerEvent event) {
        if (deezerSync != null)
            deezerSync.setVisible(false);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NoArtistsEvent event){
        if (emptyArtist != null)
            emptyArtist.setVisible(false);
    }
}
