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
import com.squareup.picasso.Picasso;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ReleasesListAdapter extends ArrayAdapter<Release> implements StickyListHeadersAdapter {

    private String[] dates;
    private final Context context;
    private long headerId = 0;

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
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.artist = (TextView) convertView.findViewById(R.id.artist);
            holder.date = (TextView) convertView.findViewById(R.id.date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(release.getTitle());
        holder.artist.setText(release.getArtist());
        holder.date.setText(dates[position]);


        try {
            Picasso.with(context)
                    .load(release.getImageUri())
                    .error(R.drawable.no_image)
                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                    .centerCrop()
                    .tag(context)
                    .into(holder.thumbnail); }
        catch (java.lang.IllegalArgumentException e){
            Picasso.with(context)
                    .load(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                    .centerCrop()
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
            holder.header = (TextView) convertView.findViewById(R.id.header);
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
        return headerId++;
    }
}
