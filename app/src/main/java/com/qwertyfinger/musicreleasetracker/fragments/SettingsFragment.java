package com.qwertyfinger.musicreleasetracker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.qwertyfinger.musicreleasetracker.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String WIFI_ONLY = "wifi_only";
    public static final String SYNC_FREQUENCY = "sync_frequency";
//    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String NOTIFICATIONS = "notifications";
    public static final String LAST_FM_USERNAME = "last_fm_username";
    public static final String LAST_FM_PASSWORD = "last_fm_password";
    public static final String DEEZER_USERNAME = "deezer_username";
    public static final String DEEZER_PASSWORD = "deezer_password";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference connectionPref = findPreference(SYNC_FREQUENCY);
        String value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SYNC_FREQUENCY, "");
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SYNC_FREQUENCY)) {
            Preference connectionPref = findPreference(key);
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
        }
    }
}
