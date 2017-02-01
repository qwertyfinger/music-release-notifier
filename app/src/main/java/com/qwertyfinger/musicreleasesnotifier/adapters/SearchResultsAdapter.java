package com.qwertyfinger.musicreleasesnotifier.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
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
import com.qwertyfinger.musicreleasesnotifier.database.DatabaseHandler;
import com.qwertyfinger.musicreleasesnotifier.entities.Artist;
import com.qwertyfinger.musicreleasesnotifier.events.ReleaseAdapterEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistAddedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistDeletedEvent;
import com.qwertyfinger.musicreleasesnotifier.events.artist.ArtistImageLoadedEvent;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.AddArtistsJob;
import com.qwertyfinger.musicreleasesnotifier.jobs.artist.DeleteArtistJob;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class SearchResultsAdapter extends ArrayAdapter<Artist> {

    private JobManager jobManager;

    public SearchResultsAdapter(Context c, List<Artist> results){
        super(c, 0, results);
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result, parent, false);

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

        DatabaseHandler db = DatabaseHandler.getInstance(getContext());

        if (db.isArtistAdded(artist.getId())) {
            holder.addButton.setVisibility(View.GONE);
            holder.removeButton.setVisibility(View.VISIBLE);

            if (Utils.isExternalStorageReadable()) {
                File thumbnail = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), artist.getId() + "" +
                        ".jpg");

                Picasso.with(getContext())
                        .load(thumbnail)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .error(R.drawable.no_artist_image)
                        .tag(getContext())
                        .into(holder.thumbnail);
            } else {
                try {
                    Picasso.with(getContext())
                            .load(artist.getImage())
                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                            .config(Bitmap.Config.RGB_565)
                            .error(R.drawable.no_artist_image)
                            .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                            .centerCrop()
                            .tag(getContext())
                            .into(holder.thumbnail);
                }
                catch (java.lang.IllegalArgumentException e){
                    Picasso.with(getContext())
                            .load(R.drawable.no_artist_image)
                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                            .config(Bitmap.Config.RGB_565)
                            .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                            .centerCrop()
                            .tag(getContext())
                            .into(holder.thumbnail);
                }
            }
        }
        else {
            holder.removeButton.setVisibility(View.GONE);
            holder.addButton.setVisibility(View.VISIBLE);
            holder.addButton.setEnabled(false);
            holder.addButton.setClickable(false);

            try {
                Picasso.with(getContext())
                        .load(artist.getImage())
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .config(Bitmap.Config.RGB_565)
                        .error(R.drawable.no_artist_image)
                        .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                        .centerCrop()
                        .tag(getContext())
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
                Picasso.with(getContext())
                        .load(R.drawable.no_artist_image)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .config(Bitmap.Config.RGB_565)
                        .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                        .centerCrop()
                        .tag(getContext())
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
                if (!Utils.isSyncInProgress(getContext()))
                    jobManager.addJobInBackground(new DeleteArtistJob(getContext(), new Artist(artist.getId(), artist.getTitle()
                            , artist.getId() + ".jpg"), view));
                else
                    Utils.makeSyncToast(getContext());
            }
        });

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isSyncInProgress(getContext())) {
                    List<Artist> artists = new ArrayList<>();
                    artists.add(artist);
                    jobManager.addJobInBackground(new AddArtistsJob(getContext(), artists, view));
                }
                else
                    Utils.makeSyncToast(getContext());
            }
        });

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
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

    public void onEvent(ReleaseAdapterEvent event) {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}
