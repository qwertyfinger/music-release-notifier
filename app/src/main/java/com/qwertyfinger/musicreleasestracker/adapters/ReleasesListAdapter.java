package com.qwertyfinger.musicreleasestracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.misc.Release;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ReleasesListAdapter extends ArrayAdapter<Release> implements StickyListHeadersAdapter {

    private String[] dates;
    private final Context context;

    public ReleasesListAdapter(Context context, List<Release> releases) {
        super(context, 0, releases);
        this.context = context;
        dates = new String[releases.size()];
        for (int i = 0; i < releases.size(); i++){
            dates[i] = releases.get(i).getDate();
        }
    }

    class ViewHolder {
        public ImageView thumbnail;
        public TextView title;
        public TextView artist;
    }

    class HeaderViewHolder {
        public TextView header;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder holder = null;
        final Release release = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.release, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.releaseCover);
            holder.title = (TextView) convertView.findViewById(R.id.releaseTitle);
            holder.artist = (TextView) convertView.findViewById(R.id.artist);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(release.getTitle());
        holder.artist.setText("by " + release.getArtist());

        File thumbnail = context.getFileStreamPath(release.getImage());

        Picasso.with(context)
            .load(thumbnail)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .error(R.drawable.no_image)
            .tag(context)
            .into(holder.thumbnail);

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.releases_header, parent, false);
            holder.header = (TextView) convertView.findViewById(R.id.releaseHeader);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String headerText = "" + dates[position];
        holder.header.setText(headerText);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
        Date date = null;

        try {
            date = formatter.parse(dates[position]);
        } catch (ParseException e) {}

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return Long.parseLong(calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH) + "" + calendar.get(Calendar.DAY_OF_MONTH));
    }
}
