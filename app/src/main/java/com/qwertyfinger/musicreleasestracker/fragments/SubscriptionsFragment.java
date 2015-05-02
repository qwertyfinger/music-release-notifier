package com.qwertyfinger.musicreleasestracker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qwertyfinger.musicreleasestracker.R;

public class SubscriptionsFragment extends Fragment{


    public static SubscriptionsFragment newInstance() {
        Bundle args = new Bundle();
        SubscriptionsFragment fragment = new SubscriptionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        return view;
    }

    /*@Override
    public void onDestroy(){
        super.onDestroy();
        *//*try {
            EventBus.getDefault().unregister(this);
        }
        catch (Throwable t){
            //in case registration didn't go through
        }*//*
    }*/
}
