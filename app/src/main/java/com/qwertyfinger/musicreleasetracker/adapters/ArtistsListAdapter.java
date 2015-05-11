package com.qwertyfinger.musicreleasetracker.adapters;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.path.android.jobqueue.JobManager;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.jobs.DeleteArtistJob;
import com.qwertyfinger.musicreleasetracker.misc.Utils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ArtistsListAdapter extends ArrayAdapter<Artist> implements StickyListHeadersAdapter {

    private List<Artist> artists;
    private final Context context;
    private JobManager jobManager;

    public ArtistsListAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);
        this.context = context;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.artist, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.artistImage);
            holder.title = (TextView) convertView.findViewById(R.id.artistTitle);
            holder.removeButton = (ImageButton) convertView.findViewById(R.id.removeButton);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.findViewById(R.id.addButton).setVisibility(View.GONE);
        holder.title.setText(artist.getTitle());

        final View view = convertView;

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobManager.addJobInBackground(new DeleteArtistJob(context, artist, view));
            }
        });

        if (Utils.isExternalStorageReadable()) {
            File thumbnail = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), artist.getImage());

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
            convertView = LayoutInflater.from(context).inflate(R.layout.artists_header, parent, false);
            holder.header = (TextView) convertView.findViewById(R.id.artistHeader);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String headerText = "" + artists.get(position).getTitle().subSequence(0, 1).charAt(0);
        holder.header.setText(headerText);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return artists.get(position).getTitle().subSequence(0, 1).charAt(0);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
