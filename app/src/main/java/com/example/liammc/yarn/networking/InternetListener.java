package com.example.liammc.yarn.networking;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.liammc.yarn.core.InitializationActivity;
import com.example.liammc.yarn.dialogs.InternetDialog;

public class InternetListener{

    private final String TAG =  "InternetListener";
    public InternetDialog internetDialog;
    NetworkChangeReceiver networkChangeReceiver;
    NetworkCallback networkCallback;
    AppCompatActivity activity;

    private boolean lostConnection = false;


    public InternetListener(AppCompatActivity _activity){
        this.activity = _activity;
        init(_activity);
    }

    //region init

    private void init(AppCompatActivity _activity){

        internetDialog = new InternetDialog();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){

            Log.d(TAG,"API level is below Lollipop so register network receiver");

            try{
                networkChangeReceiver =  new NetworkChangeReceiver(_activity,this,internetDialog);
                _activity.registerReceiver(networkChangeReceiver, networkChangeReceiver.intentFilter);
            }
            catch (IllegalArgumentException e){
                _activity.unregisterReceiver(networkChangeReceiver);
                _activity.registerReceiver(networkChangeReceiver, networkChangeReceiver.intentFilter);
            }
        }
        else{
            Log.d(TAG,"API level is or above Lollipop so enable a network callback");

            networkCallback = new NetworkCallback(_activity,internetDialog);
            networkCallback.enable(_activity);
        }

    }

    //endregion

    //region Public Static Methods

    public boolean isConnected(){

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //There is a connection
        if (activeNetwork != null) {
            lostConnection = false;
            return true;
        } else {
            if(activity instanceof InitializationActivity){
                if(lostConnection)((InitializationActivity)activity).reinitialize();
            }
            lostConnection = true;
            return false;
        }
    }


    //endregion


    //region Network Callback Class

    @TargetApi(21)
    private class NetworkCallback extends ConnectivityManager.NetworkCallback{

        private final String TAG =  "NetworkCallback";
        final NetworkRequest networkRequest;
        InternetDialog internetDialog;
        FragmentManager fm;
        AppCompatActivity activity;

        public NetworkCallback(AppCompatActivity _activity, InternetDialog _internetDialog) {
            this.activity = _activity;
            this.fm = _activity.getSupportFragmentManager();
            this.internetDialog = _internetDialog;
            this.networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        }

        public void enable(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(networkRequest , this);
        }

        @Override
        public void onAvailable(Network network) { gainedConnection();}

        @Override
        public void onLosing(Network network, int maxMsToLive){ lostConnection(); }

        @Override
        public void onLost(Network network) {lostConnection(); }

        @Override
        public void onUnavailable(){lostConnection(); }

        //region Private Methods

        private void gainedConnection(){
            try {
                internetDialog.dismissDialog(activity);
                if(activity instanceof InitializationActivity){
                    if(lostConnection)((InitializationActivity)activity).reinitialize();
                }
                lostConnection = false;
            } catch (Exception ex) {
                // Here we are logging the exception to see why it happened.
                Log.e(TAG, ex.toString());
            }
        }

        private void lostConnection(){
            try {
                internetDialog.alert(fm,TAG);
                lostConnection = true;
            } catch (Exception ex) {
                // Here we are logging the exception to see why it happened.
                Log.e(TAG, ex.toString());
            }
        }

        //endregion
    }

    //endregion

    //region Network Change Receiver Class
    private class NetworkChangeReceiver extends BroadcastReceiver{

        final String TAG = "NetworkChangeReceiver";
        InternetListener internetListener;
        InternetDialog internetDialog;
        FragmentManager fm;
        AppCompatActivity activity;

        public NetworkChangeReceiver(AppCompatActivity _activity,InternetListener _internetListener,
                                     InternetDialog _internetDialog){
            this.internetDialog = _internetDialog;
            this.internetListener = _internetListener;
            this.activity = _activity;
            this.fm = _activity.getSupportFragmentManager();
        }

        public IntentFilter intentFilter;
        {
            intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if(!internetListener.isConnected()) internetDialog.alert(fm,TAG);
            else internetDialog.dismissDialog(activity);
        }
    }
    //endregion
}
