package com.qwertyfinger.musicreleasestracker;

import android.app.Application;
import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

public class App  extends Application{
    private static App instance;
    private JobManager jobManager;

    public App(){
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    public JobManager getJobManager(){
        return jobManager;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        configureJobManager();
    }

    private void configureJobManager() {
        Configuration config = new Configuration.Builder(this).customLogger(new CustomLogger() {
            private static final String TAG = "JOBS";
            @Override
            public boolean isDebugEnabled() {
                return true;
            }

            @Override
            public void d(String text, Object... args) {
                Log.d(TAG, String.format(text, args));
            }

            @Override
            public void e(Throwable t, String text, Object... args) {
                Log.e(TAG, String.format(text, args), t);
            }

            @Override
            public void e(String text, Object... args) {
                Log.e(TAG, String.format(text, args));
            }
        })
        .build();
        jobManager = new JobManager(this, config);
    }

}
