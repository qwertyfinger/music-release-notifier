package com.qwertyfinger.musicreleasetracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasetracker.jobs.sync.SyncLastfmJob;
import com.qwertyfinger.musicreleasetracker.misc.Constants;
import com.qwertyfinger.musicreleasetracker.misc.Utils;

public class LastfmSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((!Utils.isWifiOnly(context) || Utils.isWifiConnected(context)) && Utils.isExternalStorageWritable() &&
                !Utils.isSyncInProgress(context))
            App.getInstance().getJobManager().addJobInBackground(new SyncLastfmJob(context, Constants.SCHEDULED_SYNC));
        else
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                    .LASTFM_SYNC_DELAYED, true).apply();
    }
}
