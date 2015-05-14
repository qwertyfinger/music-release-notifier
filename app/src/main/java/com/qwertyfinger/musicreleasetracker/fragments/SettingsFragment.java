package com.qwertyfinger.musicreleasetracker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.deezer.sdk.model.User;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.Utils;
import com.qwertyfinger.musicreleasetracker.jobs.sync.SyncDeezerJob;
import com.qwertyfinger.musicreleasetracker.jobs.sync.SyncLastfmJob;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String WIFI_ONLY = "wifi_only";
    public static final String SYNC_FREQUENCY = "sync_frequency";
    public static final String SYNC_SUMMARY = "sync_summary";
//    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String NOTIFICATIONS = "notifications";

    public static final String LAST_FM = "last_fm";
    public static final String LAST_FM_THRESHOLD = "last_fm_threshold";
    public static final String LAST_FM_SUMMARY = "last_fm_summary";

    public static final String DEEZER = "deezer";
    public static final String DEEZER_SUMMARY = "deezer_summary";

    private JobManager jobManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        jobManager = App.getInstance().getJobManager();

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Preference connectionPref = findPreference(SYNC_FREQUENCY);
        connectionPref.setSummary(settings.getString(SYNC_SUMMARY, "Every 5 days"));

        connectionPref = findPreference(DEEZER);
        connectionPref.setSummary(settings.getString(DEEZER_SUMMARY, ""));

        connectionPref = findPreference(LAST_FM);
        connectionPref.setSummary(settings.getString(LAST_FM_SUMMARY, ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference connectionPref = findPreference(key);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = settings.edit();
        if (key.equals(SYNC_FREQUENCY)) {
            String value = sharedPreferences.getString(key, "");
            switch (value) {
                case "0":
                    connectionPref.setSummary("Never");
                    break;
                case "7":
                    connectionPref.setSummary("Weekly");
                    break;
                case "5":
                    connectionPref.setSummary("Every 5 days");
                    break;
                case "3":
                    connectionPref.setSummary("Every 3 days");
                    break;
                case "1":
                    connectionPref.setSummary("Daily");
                    break;
                default:
                    connectionPref.setSummary("");
                    break;
            }
            editor.putString(SYNC_SUMMARY, connectionPref.getSummary().toString());
            editor.commit();
        }

        if (key.equals(DEEZER)) {
            if (sharedPreferences.getBoolean(DEEZER, false)) {
                RequestListener listener = new JsonRequestListener() {
                    @Override
                    public void onResult(Object result, Object requestId) {
                        User user = (User) result;
                        connectionPref.setSummary(user.getName());
                        editor.putString(DEEZER_SUMMARY, user.getName());
                        editor.commit();
                    }

                    @Override
                    public void onUnparsedResult(String requestResponse, Object requestId) {

                    }

                    @Override
                    public void onException(Exception e, Object requestId) {

                    }
                };

                DeezerRequest request = DeezerRequestFactory.requestCurrentUser();
                App.getInstance().getDeezerConnect().requestAsync(request, listener);

                if ((!Utils.isWifiOnly(getActivity()) || Utils.isWifiConnected(getActivity())) && Utils.isExternalStorageWritable())
                    jobManager.addJobInBackground(new SyncDeezerJob());
            }
            else {
                connectionPref.setSummary("");
                editor.putString(DEEZER_SUMMARY, "");
                editor.commit();
            }
        }

        if (key.equals(LAST_FM)){
            connectionPref.setSummary(settings.getString(LAST_FM, ""));
            editor.putString(LAST_FM_SUMMARY, connectionPref.getSummary().toString());
            editor.commit();

            if (!settings.getString(key, "").equals(""))
                jobManager.addJobInBackground(new SyncLastfmJob(getActivity()));
        }

    }

}
