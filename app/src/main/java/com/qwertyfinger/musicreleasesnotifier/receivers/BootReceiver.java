/*
 * MIT License
 *
 * Copyright (c) 2017 Andriy Chubko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
