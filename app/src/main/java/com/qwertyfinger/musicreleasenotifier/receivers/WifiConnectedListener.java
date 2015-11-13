package com.qwertyfinger.musicreleasetracker.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasetracker.misc.Utils;

public class WifiConnectedListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();

            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            int frequency = Integer.parseInt(settings.getString(SettingsFragment.SYNC_FREQUENCY, "5"));
            if (frequency != 0 && !settings.getBoolean(SettingsFragment.WIFI_ONLY, true) || Utils.isWifiConnected
                        (context)) {
                if (settings.getBoolean(SettingsFragment.RELEASE_REFRESH_DELAYED, false)) {
                    Intent receiverIntent = new Intent(context, ReleasesRefreshReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);
                    alarmMgr.cancel(alarmIntent);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils.generateRandom()
                                    * 60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils.generateRandom()
                                    * 60000, alarmIntent);

                    editor.putBoolean(SettingsFragment.RELEASE_REFRESH_DELAYED, false);
                }

                if (settings.getBoolean(SettingsFragment.DEEZER_SYNC_DELAYED, false)) {
                    Intent receiverIntent = new Intent(context, DeezerSyncReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);
                    alarmMgr.cancel(alarmIntent);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils.generateRandom()
                                    * 60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils.generateRandom()
                                    * 60000, alarmIntent);

                    editor.putBoolean(SettingsFragment.DEEZER_SYNC_DELAYED, false);
                }

                if (settings.getBoolean(SettingsFragment.LASTFM_SYNC_DELAYED, false)) {
                    Intent receiverIntent = new Intent(context, LastfmSyncReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);
                    alarmMgr.cancel(alarmIntent);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils.generateRandom()
                                    * 60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom() * 60000 - Utils.generateRandom()
                                    * 60000, alarmIntent);

                    editor.putBoolean(SettingsFragment.LASTFM_SYNC_DELAYED, false);
                }
            }
            editor.apply();
        }
    }
}
