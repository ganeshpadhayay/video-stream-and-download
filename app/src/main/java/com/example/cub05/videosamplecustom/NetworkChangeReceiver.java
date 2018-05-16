package com.example.cub05.videosamplecustom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * Created by cub05 on 5/15/2018.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    NetworkChangeCallback networkChangeCallback;
    public NetworkChangeReceiver(MainActivity mainActivity) {
        this.networkChangeCallback=mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (isOnline(context)) {
                networkChangeCallback.onNetworkChange(true);
            } else {
                networkChangeCallback.onNetworkChange(false);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface NetworkChangeCallback{
        void onNetworkChange(boolean internetPresent);
    }
}