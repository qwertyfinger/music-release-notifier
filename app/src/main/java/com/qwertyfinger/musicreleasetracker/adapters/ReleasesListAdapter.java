package com.qwertyfinger.musicreleasetracker.adapters;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.misc.Release;
import com.qwertyfinger.musicreleasetracker.util.Utils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ReleasesListAdapter extends ArrayAdapter<Release> implements StickyListHeadersAdapter {

    private List<Release> releases;
    private final Context context;

    public ReleasesListAdapter(Context context, List<Release> releases) {
        super(context, 0, releases);
        this.context = context;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.release, parent, false);

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

        if (Utils.isExternalStorageReadable()) {
            File thumbnail = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), release.getImage());

            Picasso.with(context)
                    .load(thumbnail)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .error(R.drawable.no_image)
                    .tag(context)
                    .into(holder.thumbnail);
        }
        else {
            Picasso.with(context)
                    .load(R.drawable.no_image)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .tag(context)
                    .into(holder.thumbnail);
        }

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

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
        Date date = null;

        try {
            date = formatter.parse(releases.get(position).getDate());
        } catch (ParseException e) {}

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String headerText = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, context.getResources().getConfiguration().locale);

        if (Locale.getDefault().getLanguage().equals("uk"))
            headerText = Utils.convertMonth(context, true, calendar.get(Calendar.MONTH));

        if (Locale.getDefault().getLanguage().equals("ru"))
            headerText = Utils.convertMonth(context, false, calendar.get(Calendar.MONTH));

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
