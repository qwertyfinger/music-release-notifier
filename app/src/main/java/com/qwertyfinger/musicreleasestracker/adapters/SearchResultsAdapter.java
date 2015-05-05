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
import com.qwertyfinger.musicreleasestracker.jobs.AddArtistJob;
import com.qwertyfinger.musicreleasestracker.misc.SearchResult;
import com.squareup.picasso.Picasso;

import java.util.List;


public class SearchResultsAdapter extends ArrayAdapter<SearchResult> {

    private final Context context;
    private JobManager jobManager;

    public SearchResultsAdapter(Context c, List<SearchResult> results){
        super(c, 0, results);
        context = c;
        jobManager = App.getInstance().getJobManager();
    }

    class ViewHolder {
        public ImageView thumbnail;
        public TextView title;
        public ImageButton control;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        final SearchResult searchResult = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.search_result, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.control = (ImageButton) convertView.findViewById(R.id.control);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(searchResult.getTitle());
//        holder.control.setImageDrawable(null);
        holder.control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobManager.addJobInBackground(new AddArtistJob(context, searchResult));
            }
        });
        try {
            Picasso.with(context)
                    .load(searchResult.getImageUrl())
                    .config(Bitmap.Config.RGB_565)
                    .error(R.drawable.no_image)
                    .resizeDimen(R.dimen.search_result_list_image_size, R.dimen.search_result_list_image_size)
                    .centerCrop()
                    .tag(context)
                    .into(holder.thumbnail); }
        catch (java.lang.IllegalArgumentException e){
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
}
