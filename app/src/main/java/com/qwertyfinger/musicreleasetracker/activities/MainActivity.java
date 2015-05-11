package com.qwertyfinger.musicreleasetracker.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.Constants;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.adapters.MyFragmentPagerAdapter;
import com.qwertyfinger.musicreleasetracker.jobs.RefreshReleasesJob;
import com.qwertyfinger.musicreleasetracker.misc.Utils;

import java.io.File;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {

    JobManager jobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File file = new File(getFilesDir(), "spaceOdyssey");
        if (!file.exists()) {
            try {
                Utils.generateKey(this);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        if (!Utils.isExternalStorageWritable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        jobManager = App.getInstance().getJobManager();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);
        tabsStrip.setBackgroundColor(Color.parseColor("#228B22"));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
            default:
                return super.onOptionsItemSelected(item);
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
