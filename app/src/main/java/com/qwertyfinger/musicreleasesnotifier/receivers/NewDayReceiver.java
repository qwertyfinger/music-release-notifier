package com.qwertyfinger.musicreleasesnotifier.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.ShowNotificationJob;
import com.qwertyfinger.musicreleasesnotifier.jobs.release.UpdateReleasesJob;

public class NewDayReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getInstance().getJobManager().addJobInBackground(new UpdateReleasesJob(context));

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(SettingsFragment.SHOWN_RELEASES,
                null);
        App.getInstance().getJobManager().addJobInBackground(new ShowNotificationJob(context));
    }
}
