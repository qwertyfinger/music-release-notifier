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

package com.qwertyfinger.musicreleasesnotifier.jobs.release;

import android.content.Context;
import android.os.Environment;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.events.release.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;

public class EmptyReleasesJob extends Job{
    private final Context context;

    public EmptyReleasesJob(Context context) {
        super(new Params(Constants.JOB_PRIORITY_HIGH).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
    }


    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (db.getReleasesCount() > 0) {
            List<Release> releases = db.getAllReleases();
            for (Release release: releases){
                File image = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), release.getImage());
                image.delete();
            }

            db.deleteAllReleases();
            EventBus.getDefault().post(new ReleasesChangedEvent());
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
