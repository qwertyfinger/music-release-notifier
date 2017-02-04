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

package com.qwertyfinger.musicreleasesnotifier.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.entities.Release;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;
import com.squareup.picasso.Picasso;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReleasesListAdapter extends ArrayAdapter<Release> implements StickyListHeadersAdapter {

    private List<Release> releases;

    public ReleasesListAdapter(Context context, List<Release> releases) {
        super(context, 0, releases);
        this.releases = releases;
    }

    private class ViewHolder {
        public ImageView thumbnail;
        public TextView title;
        public TextView artist;
        public TextView date;
    }

    class HeaderViewHolder {
        public TextView header;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder holder = null;
        final Release release = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.release, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.releaseCover);
            holder.title = (TextView) convertView.findViewById(R.id.releaseTitle);
            holder.artist = (TextView) convertView.findViewById(R.id.artistName);
            holder.date = (TextView) convertView.findViewById(R.id.date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(release.getTitle());
        holder.artist.setText("by " + release.getArtist());
        holder.date.setText(release.getDate());


        try {
            Picasso.with(getContext())
                    .load(release.getImage())
                    .config(Bitmap.Config.RGB_565)
                    .centerCrop()
                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                    .error(R.drawable.no_album_image)
                    .tag(getContext())
                    .into(holder.thumbnail);
        } catch (Exception e) {
            Picasso.with(getContext())
                    .load(R.drawable.no_album_image)
                    .config(Bitmap.Config.RGB_565)
                    .centerCrop()
                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size).tag(getContext())
                    .into(holder.thumbnail);
        }
        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.releases_header, parent, false);
            holder.header = (TextView) convertView.findViewById(R.id.releaseHeader);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
        Date date = null;

        try {
            date = formatter.parse(releases.get(position).getDate());
        } catch (ParseException e) {}

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String headerText = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, getContext().getResources().getConfiguration
                ().locale);

        if (Locale.getDefault().getLanguage().equals("uk"))
            headerText = Utils.convertMonth(getContext(), true, calendar.get(Calendar.MONTH));

        if (Locale.getDefault().getLanguage().equals("ru"))
            headerText = Utils.convertMonth(getContext(), false, calendar.get(Calendar.MONTH));

        holder.header.setText(headerText);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
        Date date = null;

        try {
            date = formatter.parse(releases.get(position).getDate());
        } catch (ParseException e) {}

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return Long.parseLong(calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH));
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
