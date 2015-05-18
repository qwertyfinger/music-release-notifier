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
import com.qwertyfinger.musicreleasetracker.Utils;
import com.qwertyfinger.musicreleasetracker.entities.Artist;
import com.qwertyfinger.musicreleasetracker.jobs.artist.DeleteArtistJob;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ArtistsListAdapter extends ArrayAdapter<Artist> implements StickyListHeadersAdapter /* Section Indexer */{

    private List<Artist> artists;
    private JobManager jobManager;
    private HashMap<String, Integer> mapIndex;
//    private String[] sections;

    public ArtistsListAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);
        jobManager = App.getInstance().getJobManager();
        this.artists = artists;

        /*mapIndex = new LinkedHashMap<String, Integer>();

        int x = 0;
        for (Artist artist: artists) {
            String ch = artist.getTitle().substring(0, 1).toUpperCase();
            ch = ch.toUpperCase();
            mapIndex.put(ch, x);
            x++;
        }

        Set<String> sectionLetters = mapIndex.keySet();

        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);

        Log.d("sectionList", sectionList.toString());
        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);*/
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

        if (Utils.isExternalStorageReadable()) {
            File thumbnail = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), artist.getImage());

            Picasso.with(getContext())
                    .load(thumbnail)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .error(R.drawable.no_artist_image)
                    .tag(getContext())
                    .into(holder.thumbnail);
        }

        else {
            Picasso.with(getContext())
                    .load(R.drawable.no_artist_image)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .tag(getContext())
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

    /*@Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex > 0) {
            int index = sectionIndex - 1;
        }
        return mapIndex.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }*/
}
