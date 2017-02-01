package com.qwertyfinger.musicreleasesnotifier.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.qwertyfinger.musicreleasesnotifier.fragments.ArtistsFragment;
import com.qwertyfinger.musicreleasesnotifier.fragments.ReleasesFragment;

public class MainFragmentPagerAdapter extends FragmentStatePagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Upcoming Releases", "Artists"};

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return (position == 0)? new ReleasesFragment() : new ArtistsFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
