package com.qwertyfinger.musicreleasestracker.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.squareup.picasso.Picasso;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ArtistsListAdapter extends ArrayAdapter<Artist> implements StickyListHeadersAdapter {

    private String[] artists;
    private final Context context;

    public ArtistsListAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);
        this.context = context;
        this.artists = new String[artists.size()];
        for (int i = 0; i < artists.size(); i++){
            this.artists[i] = artists.get(i).getTitle();
        }
    }

    class ViewHolder {
        public ImageView thumbnail;
        public TextView title;
    }

    class HeaderViewHolder {
        public TextView header;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder holder = null;
        final Artist artist = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.release, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.title = (TextView) convertView.findViewById(R.id.title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(artist.getTitle());

        try {
            Picasso.with(context)
                    .load(artist.getImage())
                    .config(Bitmap.Config.RGB_565)
                    .error(R.drawable.no_image)
                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                    .centerCrop()
                    .tag(context)
                    .into(holder.thumbnail);
        } catch (Exception e) {
            Picasso.with(context)
                    .load(R.drawable.no_image)
                    .config(Bitmap.Config.RGB_565)
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

        String headerText = "" + artists[position].subSequence(0, 1).charAt(0);
        holder.header.setText(headerText);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return artists[position].subSequence(0, 1).charAt(0);
    }
}
