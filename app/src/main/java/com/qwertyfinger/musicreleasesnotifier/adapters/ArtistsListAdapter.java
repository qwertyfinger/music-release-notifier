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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.DeleteArtistJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;
import com.squareup.picasso.Picasso;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.util.HashMap;
import java.util.List;

public class ArtistsListAdapter extends ArrayAdapter<Artist> implements StickyListHeadersAdapter /* Section Indexer */{

    private List<Artist> artists;
    private JobManager jobManager;
    private HashMap<String, Integer> mapIndex;

    public ArtistsListAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);
        jobManager = App.getInstance().getJobManager();
        this.artists = artists;

    }

    private class ViewHolder {
        public ImageView thumbnail;
        public TextView title;
        public ImageButton removeButton;
    }

    class HeaderViewHolder {
        public TextView header;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder holder = null;
        final Artist artist = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.artistImage);
            holder.title = (TextView) convertView.findViewById(R.id.artistTitle);
            holder.removeButton = (ImageButton) convertView.findViewById(R.id.removeButton);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(artist.getTitle());

        final View view = convertView;
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isSyncInProgress(getContext()))
                    jobManager.addJobInBackground(new DeleteArtistJob(getContext(), artist, view));
                else
                    Utils.makeSyncToast(getContext());
            }
        });


        try {
            Picasso.with(getContext())
                    .load(artist.getImage())
                    .config(Bitmap.Config.RGB_565)
                    .centerCrop()
                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                    .error(R.drawable.no_artist_image)
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artists_header, parent, false);
            holder.header = (TextView) convertView.findViewById(R.id.artistHeader);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String headerText = "" + artists.get(position).getTitle().toUpperCase().subSequence(0, 1).charAt(0);
        holder.header.setText(headerText);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return artists.get(position).getTitle().toUpperCase().subSequence(0, 1).charAt(0);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

}
