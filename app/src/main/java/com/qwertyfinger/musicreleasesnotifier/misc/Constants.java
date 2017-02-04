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

package com.qwertyfinger.musicreleasesnotifier.misc;

public class Constants {
    public static final String JOB_SYNC_TAG = "sync";

    public static final int EXPLICIT_REFRESH = 0;
    public static final int AFTER_ADDING_REFRESH = 1;
    public static final int SCHEDULED_REFRESH = 2;
    public static final int AFTER_SYNC_REFRESH = 3;

    public static final int EXPLICIT_SYNC = 0;
    public static final int SCHEDULED_SYNC = 1;

    public static final int ARTIST_USER_ADD = 0;
    public static final int ARTIST_SYNC_ADD = 1;

    public static final int JOB_PRIORITY_LOW = 1;
    public static final int JOB_PRIORITY_MEDIUM = 2;
    public static final int JOB_PRIORITY_HIGH = 3;
    public static final int JOB_PRIORITY_CRITICAL = 4;

    public static final String JOB_GROUP_DATABASE = "database";
    public static final String JOB_GROUP_SYNC = "sync";

    public static final String LASTFM_USER_AGENT = "Music Release Notifier / Andriy Chubko (andriy.chubko.q@gmail.com)";

    public static final String TYPE_ALBUM = "album";
//    public static final String TYPE_EP = "ep";
//    public static final String TYPE_SINGLE = "single";
//    public static final String TYPE_COMPILATION = "compilation";
//    public static final String TYPE_SOUNDTRACK = "soundtrack";
//    public static final String TYPE_SPOKENWORD = "spokenword";
//    public static final String TYPE_INTERVIEW = "interview";
//    public static final String TYPE_AUDIOBOOK = "audiobook";
//    public static final String TYPE_LIVE = "live";
//    public static final String TYPE_REMIX = "remix";
//    public static final String TYPE_OTHER = "other";
}
