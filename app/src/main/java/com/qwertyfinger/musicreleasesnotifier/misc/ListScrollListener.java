package com.qwertyfinger.musicreleasesnotifier.misc;

import android.content.Context;
import android.widget.AbsListView;

import com.squareup.picasso.Picasso;

public class ListScrollListener implements AbsListView.OnScrollListener {

    private final Context context;

    public ListScrollListener(Context context) {
        this.context = context;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        final Picasso picasso = Picasso.with(context);
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            picasso.resumeTag(this);
        } else {
            picasso.pauseTag(this);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //do nothing
    }
}
