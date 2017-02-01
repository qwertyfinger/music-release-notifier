package com.qwertyfinger.musicreleasesnotifier.fragments;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.events.lastfm.LoggedInLastfmEvent;
import com.qwertyfinger.musicreleasesnotifier.events.lastfm.LoggedOutLastfmEvent;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.ShowNotificationJob;
import com.qwertyfinger.musicreleasesnotifier.jobs.sync.SyncDeezerJob;
import com.qwertyfinger.musicreleasesnotifier.jobs.sync.SyncLastfmJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;
import com.qwertyfinger.musicreleasesnotifier.receivers.DeezerSyncReceiver;
import com.qwertyfinger.musicreleasesnotifier.receivers.LastfmSyncReceiver;
import com.qwertyfinger.musicreleasesnotifier.receivers.ReleasesRefreshReceiver;

import java.util.HashSet;
import java.util.Set;

import de.greenrobot.event.EventBus;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String WIFI_ONLY = "wifi_only";
    public static final String SYNC_FREQUENCY = "sync_frequency";
    public static final String SYNC_SUMMARY = "sync_summary";
//    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String NOTIFICATIONS = "notifications";

    public static final String LAST_FM = "last_fm";
    public static final String LAST_FM_THRESHOLD = "last_fm_threshold";
    public static final String LAST_FM_SUMMARY = "last_fm_summary";
    public static final String LAST_FM_FLAG = "last_fm_flag";
    public static final String LASTFM_SYNC_ALARM_SET = "lastfm_sync_alarm_set";
    public static final String LASTFM_SYNC_DELAYED = "lastfm_sync_delayed";


    public static final String DEEZER = "deezer";
    public static final String DEEZER_SUMMARY = "deezer_summary";
    public static final String DEEZER_SYNC_ALARM_SET = "deezer_sync_alarm_set";
    public static final String DEEZER_SYNC_DELAYED = "deezer_sync_delayed";

    public static final String NEWDAY_ALARM_SET = "newday_alarm_set";

    public static final String RELEASE_REFRESH_ALARM_SET = "release_refresh_alarm_set";
    public static final String RELEASE_REFRESH_DELAYED = "release_refresh_delayed";

    public static final String SHOWN_RELEASES = "shown_releases";

    public static final String SYNC_IN_PROGRESS = "sync_in_progress";

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
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = settings.edit();

        if (key.equals(SYNC_FREQUENCY)) {
            String value = sharedPreferences.getString(key, "");

            AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            if (settings.getBoolean(SettingsFragment.DEEZER_SYNC_ALARM_SET, false)) {
                Intent receiverIntent = new Intent(getActivity(), DeezerSyncReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, receiverIntent, 0);
                alarmMgr.cancel(alarmIntent);
            }
            if (settings.getBoolean(SettingsFragment.LASTFM_SYNC_ALARM_SET, false)) {
                Intent receiverIntent = new Intent(getActivity(), LastfmSyncReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, receiverIntent, 0);
                alarmMgr.cancel(alarmIntent);
            }
            if (settings.getBoolean(SettingsFragment.RELEASE_REFRESH_ALARM_SET, false)) {
                Intent receiverIntent = new Intent(getActivity(), ReleasesRefreshReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, receiverIntent, 0);
                alarmMgr.cancel(alarmIntent);
            }

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

            int frequency = Integer.parseInt(value);
            if (frequency != 0) {
                if (settings.getBoolean(SettingsFragment.DEEZER, false)) {
                    Intent receiverIntent = new Intent(getActivity(), DeezerSyncReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0,
                            receiverIntent,
                            0);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000, alarmIntent);
                    editor.putBoolean(SettingsFragment.DEEZER_SYNC_ALARM_SET, true);
                }

                if (settings.getBoolean(SettingsFragment.LAST_FM_FLAG, false)) {
                    Intent receiverIntent = new Intent(getActivity(), LastfmSyncReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0,
                            receiverIntent, 0);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils
                                    .generateRandom()
                                    *60000, alarmIntent);
                    editor.putBoolean(SettingsFragment.LASTFM_SYNC_ALARM_SET, true);
                }

                if (settings.getBoolean(SettingsFragment.RELEASE_REFRESH_ALARM_SET, false)) {
                    Intent receiverIntent = new Intent(getActivity(), ReleasesRefreshReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0,
                            receiverIntent, 0);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000, alarmIntent);

                    editor.putBoolean(SettingsFragment.RELEASE_REFRESH_ALARM_SET, true);
                }
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


                        int frequency = Integer.parseInt(settings.getString(SettingsFragment.SYNC_FREQUENCY, "5"));
                        if (frequency != 0) {
                            AlarmManager alarmMgr = (AlarmManager)
                                    getActivity().getSystemService(Context.ALARM_SERVICE);
                            Intent receiverIntent = new Intent(getActivity(), DeezerSyncReceiver.class);
                            PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0,
                                    receiverIntent, 0);

                            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils
                                            .generateRandom() * 60000,
                                    frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils
                                            .generateRandom() * 60000, alarmIntent);
                            editor.putBoolean(SettingsFragment.DEEZER_SYNC_ALARM_SET, true);
                        }

                        if ((!Utils.isWifiOnly(getActivity()) || Utils.isWifiConnected(getActivity()))
                                && Utils.isExternalStorageWritable())
                            jobManager.addJobInBackground(new SyncDeezerJob(getActivity(), Constants.SCHEDULED_SYNC));
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
            }
            else {
                connectionPref.setSummary("");
                editor.putString(DEEZER_SUMMARY, "");
                editor.commit();

                if (settings.getBoolean(SettingsFragment.DEEZER_SYNC_ALARM_SET, false)) {
                    AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    Intent receiverIntent = new Intent(getActivity(), DeezerSyncReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0,
                            receiverIntent, 0);
                    alarmMgr.cancel(alarmIntent);
                    editor.putBoolean(SettingsFragment.DEEZER_SYNC_ALARM_SET, false);
                }
            }
        }

        if (key.equals(LAST_FM)){
            connectionPref.setSummary(settings.getString(LAST_FM, ""));
            editor.putString(LAST_FM_SUMMARY, connectionPref.getSummary().toString());
            editor.commit();


            if (settings.getString(key, "").equals("") && settings.getBoolean
                    (SettingsFragment.LASTFM_SYNC_ALARM_SET, false)) {
                AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                Intent receiverIntent = new Intent(getActivity(), LastfmSyncReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, receiverIntent,
                        0);
                alarmMgr.cancel(alarmIntent);
                editor.putBoolean(SettingsFragment.LASTFM_SYNC_ALARM_SET, false);
                editor.putBoolean(SettingsFragment.LAST_FM_FLAG, false);
                EventBus.getDefault().post(new LoggedOutLastfmEvent());
            }
            else {
                int frequency = Integer.parseInt(settings.getString(SettingsFragment.SYNC_FREQUENCY, "5"));
                if (frequency != 0) {
                    AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    Intent receiverIntent = new Intent(getActivity(), LastfmSyncReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0,
                            receiverIntent, 0);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils
                                    .generateRandom() * 60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils
                                    .generateRandom() * 60000, alarmIntent);
                    editor.putBoolean(SettingsFragment.LASTFM_SYNC_ALARM_SET, true);
                }

                if ((!Utils.isWifiOnly(getActivity()) || Utils.isWifiConnected(getActivity())) &&
                        Utils.isExternalStorageWritable())
                    jobManager.addJobInBackground(new SyncLastfmJob(getActivity(), Constants.SCHEDULED_SYNC));

                editor.putBoolean(SettingsFragment.LAST_FM_FLAG, true);
                EventBus.getDefault().post(new LoggedInLastfmEvent());
            }
        }

        if (key.equals(SettingsFragment.NOTIFICATIONS)) {
            Set<String> notifications = PreferenceManager.getDefaultSharedPreferences(getActivity()).getStringSet
                    (SettingsFragment.NOTIFICATIONS, null);

            if (notifications != null) {

                NotificationManager notifManager = (NotificationManager) getActivity().getSystemService(Context
                        .NOTIFICATION_SERVICE);

                Set<Integer> noteTime = new HashSet<>(notifications.size());
                for (String time : notifications) {
                    noteTime.add(Integer.parseInt(time));
                }
                int[] types = {0,1,7,14};
                for (int i: types) {
                    if (!noteTime.contains(i))
                        notifManager.cancel(i);
                }

                jobManager.addJobInBackground(new ShowNotificationJob(getActivity()));
            }
        }

        editor.apply();
    }

}
