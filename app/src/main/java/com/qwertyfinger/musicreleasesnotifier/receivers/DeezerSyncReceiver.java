package com.qwertyfinger.musicreleasesnotifier.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.jobs.sync.SyncDeezerJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

public class DeezerSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if ((!Utils.isWifiOnly(context) || Utils.isWifiConnected(context)) && Utils.isExternalStorageWritable() &&
                !Utils.isSyncInProgress(context)) {
            App.getInstance().getJobManager().addJobInBackground(new SyncDeezerJob(context, Constants.SCHEDULED_SYNC));
        }
        else
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                    .DEEZER_SYNC_DELAYED, true).apply();
    }
}
