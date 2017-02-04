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
