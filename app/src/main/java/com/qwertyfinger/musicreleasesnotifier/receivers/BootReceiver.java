package com.qwertyfinger.musicreleasesnotifier.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent receiverIntent = new Intent(context, NewDayReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 24);
            calendar.set(Calendar.MINUTE, 0);

            alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
            editor.putBoolean(SettingsFragment.NEWDAY_ALARM_SET, true);

            int frequency = Integer.parseInt(settings.getString(SettingsFragment.SYNC_FREQUENCY, "5"));
            if (frequency != 0) {
                if (settings.getBoolean(SettingsFragment.DEEZER, false)) {
                    receiverIntent = new Intent(context, DeezerSyncReceiver.class);
                    alarmIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000, alarmIntent);
                    editor.putBoolean(SettingsFragment.DEEZER_SYNC_ALARM_SET, true);
                }

                if (settings.getBoolean(SettingsFragment.LAST_FM_FLAG, false)) {
                    receiverIntent = new Intent(context, LastfmSyncReceiver.class);
                    alarmIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000, alarmIntent);
                    editor.putBoolean(SettingsFragment.LASTFM_SYNC_ALARM_SET, true);
                }

                if (settings.getBoolean(SettingsFragment.RELEASE_REFRESH_ALARM_SET, false)) {
                    receiverIntent = new Intent(context, NewDayReceiver.class);
                    alarmIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);

                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000,
                            frequency * AlarmManager.INTERVAL_DAY + Utils.generateRandom()*60000 - Utils.generateRandom()
                                    *60000, alarmIntent);

                    editor.putBoolean(SettingsFragment.RELEASE_REFRESH_ALARM_SET, true);
                }
            }

            editor.apply();
        }
    }
}
