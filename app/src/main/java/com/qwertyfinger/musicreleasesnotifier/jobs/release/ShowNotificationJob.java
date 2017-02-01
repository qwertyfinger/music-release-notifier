package com.qwertyfinger.musicreleasesnotifier.jobs.release;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.activities.MainActivity;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShowNotificationJob extends Job {

    private Context context;
    private List<Release> newReleases;

    public ShowNotificationJob(Context context) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
        this.newReleases = null;
    }

    public ShowNotificationJob(Context context, List<Release> newReleases) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
        this.newReleases = newReleases;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        List<Release> releases;
        if (newReleases != null)
            releases = newReleases;
        else
            releases = DatabaseHandler.getInstance(context).getAllReleases();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> notifications = settings.getStringSet(SettingsFragment.NOTIFICATIONS, null);

        if (notifications != null) {

            Set<String> retreivedReleases = settings.getStringSet(SettingsFragment.SHOWN_RELEASES, null);
            if (retreivedReleases != null) {
                for (String id: retreivedReleases) {
                    Release release = DatabaseHandler.getInstance(context).getRelease(id);
                    if (release != null && !releases.contains(release))
                        releases.add(release);
                }
            }

            Set<Integer> noteTime = new HashSet<>(notifications.size());
            for (String time: notifications) {
                noteTime.add(Integer.parseInt(time));
            }

            Map<Integer, List<Release>> items = new HashMap<>(notifications.size());
            for (int time: noteTime) {
                items.put(time, new ArrayList<Release>());
            }

            for (Release release: releases) {
                Calendar releaseDate = Calendar.getInstance();
                DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
                Date date = null;

                try {
                    date = formatter.parse(release.getDate());
                } catch (ParseException e) {
                    date = new Date();
                }

                releaseDate.setTime(date);

                for (int time: noteTime) {
                    Calendar currentDate = Calendar.getInstance();
                    currentDate.add(Calendar.DAY_OF_MONTH, time);
                    if ((releaseDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)) &&
                            (releaseDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)) &&
                            (releaseDate.get(Calendar.DAY_OF_MONTH)) == currentDate.get(Calendar.DAY_OF_MONTH))
                        items.get(time).add(release);
                }
            }

            SharedPreferences.Editor editor = settings.edit();
            Set<String> shownReleases = new HashSet<>();

            for (int time: noteTime) {
                List<Release> curReleases = items.get(time);

                if (curReleases.size() != 0) {
                    PendingIntent intent = PendingIntent.getActivity(context, 0, new
                            Intent(context, MainActivity.class), 0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setContentIntent(intent)
                            .setSmallIcon(R.drawable.music_record)
                            .setAutoCancel(true);

                    if (curReleases.size() > 1) {
                        switch (time) {
                            case 14:
                                builder.setContentTitle(curReleases.size() + " new releases are " +
                                        "coming out in two weeks: ");
                                break;
                            case 7:
                                builder.setContentTitle(curReleases.size() + " new releases are " +
                                        "coming out in a week: ");
                                break;
                            case 1:
                                builder.setContentTitle(curReleases.size() + " new releases are " +
                                        "coming out tomorrow: ");
                                break;
                            case 0:
                                builder.setContentTitle(curReleases.size() + " new releases are " +
                                        "out today: ");
                                break;
                        }
                        StringBuilder text = new StringBuilder();
                        for (Release release: curReleases) {
                            text.append("'");
                            text.append(release.getTitle());
                            text.append("', ");
                            shownReleases.add(release.getId());
                        }
                        builder.setContentText(text.toString().substring(0, text.length()-2));
                    }
                    else {
                        switch (time) {
                            case 14:
                                builder.setContentTitle("New release is coming out in two weeks: ");
                                break;
                            case 7:
                                builder.setContentTitle("New release is coming out in a week: ");
                                break;
                            case 1:
                                builder.setContentTitle("New release is coming out tomorrow: ");
                                break;
                            case 0:
                                builder.setContentTitle("New release is out today: ");
                                break;
                        }
                        builder.setContentText("'" + curReleases.get(0).getTitle() + "' by " +
                                curReleases.get(0).getArtist());
                        shownReleases.add(releases.get(0).getId());
                    }

                    NotificationManager mNotificationManager = (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(time, builder.build());

                }
            }
            editor.putStringSet(SettingsFragment.SHOWN_RELEASES, shownReleases);
            editor.apply();
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
