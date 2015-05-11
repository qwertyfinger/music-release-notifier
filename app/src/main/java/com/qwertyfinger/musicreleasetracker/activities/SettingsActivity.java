package com.qwertyfinger.musicreleasetracker.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            settingsFragment = (SettingsFragment) getFragmentManager().getFragment(savedInstanceState, "settingsFragment");
        else
            settingsFragment = new SettingsFragment();

        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        getFragmentManager().putFragment(savedInstanceState, "settingsFragment", settingsFragment);

        super.onSaveInstanceState(savedInstanceState);
    }
}