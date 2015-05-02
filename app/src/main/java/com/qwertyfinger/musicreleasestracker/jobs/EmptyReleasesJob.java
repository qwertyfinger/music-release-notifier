package com.qwertyfinger.musicreleasestracker.jobs;

import android.content.Context;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasestracker.Constants;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ReleasesChangedEvent;

import de.greenrobot.event.EventBus;

/**
 * called only when artists were emptied
 */
public class EmptyReleasesJob extends Job{
    private final Context context;

    public EmptyReleasesJob(Context context) {
        super(new Params(1).groupBy("database"));
        this.context = context;
    }


    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.deleteAllReleases();
        EventBus.getDefault().post(new ReleasesChangedEvent(Constants.AFTER_ADDING_REFRESH));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
