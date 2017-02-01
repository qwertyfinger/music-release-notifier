package com.qwertyfinger.musicreleasesnotifier.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.RefreshReleasesJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

public class ReleasesRefreshReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((!Utils.isWifiOnly(context) || Utils.isWifiConnected(context)) && Utils.isExternalStorageWritable() &&
                DatabaseHandler.getInstance(context).getArtistsCount() != 0 && !Utils.isSyncInProgress(context))
            App.getInstance().getJobManager().addJobInBackground(new RefreshReleasesJob(context,
                    Constants.SCHEDULED_REFRESH));
        else
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(SettingsFragment
                    .RELEASE_REFRESH_DELAYED, true).apply();
    }
}