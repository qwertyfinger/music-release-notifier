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

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.events.release.ReleasesChangedEvent;
import com.qwertyfinger.musicreleasesnotifier.misc.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UpdateReleasesJob extends Job {

    private final Context context;

    public UpdateReleasesJob(Context context) {
        super(new Params(Constants.JOB_PRIORITY_LOW).groupBy(Constants.JOB_GROUP_DATABASE));
        this.context = context;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (!(db.getReleasesCount() == 0)) {
            List<Release> releases = db.getAllReleases();
            Collections.sort(releases);

            boolean isChanged = false;

            for (Release release: releases) {
                Calendar currentDate = Calendar.getInstance();
                Calendar releaseDate = currentDate;
                DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
                Date date = null;

                try {
                    date = formatter.parse(release.getDate());
                } catch (ParseException e) {
                    date = new Date();
                }

                releaseDate.setTime(date);

                if (releaseDate.get(Calendar.YEAR) < currentDate.get(Calendar.YEAR))
                    db.deleteRelease(release.getId());
                else {
                    if (releaseDate.get(Calendar.MONTH) < currentDate.get(Calendar.MONTH))
                        db.deleteRelease(release.getId());
                    else {
                        if (releaseDate.get(Calendar.DAY_OF_MONTH) < currentDate.get(Calendar.DAY_OF_MONTH))
                            db.deleteRelease(release.getId());
                        else
                            break;
                    }
                }

                isChanged = true;
            }

            if (isChanged)
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
