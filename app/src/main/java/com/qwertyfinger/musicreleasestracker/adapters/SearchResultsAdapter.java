package com.qwertyfinger.musicreleasestracker.adapters;

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
import com.qwertyfinger.musicreleasestracker.App;
import com.qwertyfinger.musicreleasestracker.R;
import com.qwertyfinger.musicreleasestracker.database.DatabaseHandler;
import com.qwertyfinger.musicreleasestracker.events.ArtistAddedEvent;
import com.qwertyfinger.musicreleasestracker.events.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasestracker.events.ArtistImageLoadedEvent;
import com.qwertyfinger.musicreleasestracker.events.ReleaseAdapterEvent;
import com.qwertyfinger.musicreleasestracker.jobs.AddArtistJob;
import com.qwertyfinger.musicreleasestracker.jobs.DeleteArtistJob;
import com.qwertyfinger.musicreleasestracker.misc.Artist;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;


public class SearchResultsAdapter extends ArrayAdapter<Artist> {

    private final Context context;
    private JobManager jobManager;

    public SearchResultsAdapter(Context c, List<Artist> results){
        super(c, 0, results);
        context = c;
        jobManager = App.getInstance().getJobManager();
        EventBus.getDefault().register(this);
    }

    private class ViewHolder {
        public ImageView thumbnail;
        public TextView title;
        public ImageButton addButton;
        public ImageButton removeButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final Artist artist = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.artist, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.artistImage);
            holder.title = (TextView) convertView.findViewById(R.id.artistTitle);
            holder.addButton = (ImageButton) convertView.findViewById(R.id.addButton);
            holder.removeButton = (ImageButton) convertView.findViewById(R.id.removeButton);

            holder.addButton.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.GONE);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(artist.getTitle());

        final View view = convertView;

        DatabaseHandler db = DatabaseHandler.getInstance(context);

        if (db.isArtistAdded(artist.getId())) {
            holder.addButton.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.VISIBLE);

            File thumbnail = context.getFileStreamPath(artist.getId() + ".jpg");

            Picasso.with(context)
                    .load(thumbnail)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .error(R.drawable.no_image)
                    .tag(context)
                    .into(holder.thumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }
        else {
            holder.removeButton.setVisibility(View.GONE);
            holder.addButton.setVisibility(View.VISIBLE);
            holder.addButton.setEnabled(false);
            holder.addButton.setClickable(false);

            try {
                Picasso.with(context)
                        .load(artist.getImage())
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .config(Bitmap.Config.RGB_565)
                        .error(R.drawable.no_image)
                        .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                        .centerCrop()
                        .tag(context)
                        .into(holder.thumbnail, new Callback() {
                            @Override
                            public void onSuccess() {
                                EventBus.getDefault().post(new ArtistImageLoadedEvent(view));
                            }

                            @Override
                            public void onError() {
                                EventBus.getDefault().post(new ArtistImageLoadedEvent(view));
                            }
                        });
            }
            catch (java.lang.IllegalArgumentException e){
                Picasso.with(context)
                        .load(R.drawable.no_image)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .config(Bitmap.Config.RGB_565)
                        .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                        .centerCrop()
                        .tag(context)
                        .into(holder.thumbnail, new Callback() {
                            @Override
                            public void onSuccess() {
                                EventBus.getDefault().post(new ArtistImageLoadedEvent(view));
                            }

                            @Override
                            public void onError() {
                                EventBus.getDefault().post(new ArtistImageLoadedEvent(view));
                            }
                        });
            }
        }

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobManager.addJobInBackground(new DeleteArtistJob(context, artist, view));
            }
        });

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobManager.addJobInBackground(new AddArtistJob(context, artist, view));
            }
        });

        return convertView;
    }

    public void onEventMainThread(ArtistImageLoadedEvent event){
        View view = event.getView();
        if (view != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null) {
                holder.addButton.setEnabled(true);
                holder.addButton.setClickable(true);
            }
            view.setTag(holder);
        }
    }

    public void onEventMainThread(ArtistAddedEvent event){
        View view = event.getView();
        if (view != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null) {
                holder.addButton.setVisibility(View.GONE);
                holder.removeButton.setVisibility(View.VISIBLE);
            }
            view.setTag(holder);
        }
    }

    public void onEventMainThread(ArtistDeletedEvent event){
        View view = event.getView();
        if (view != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder != null) {
                holder.removeButton.setVisibility(View.GONE);
                holder.addButton.setVisibility(View.VISIBLE);
            }
            view.setTag(holder);
        }
    }

    public void onEventMainThread(ReleaseAdapterEvent event) {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}
