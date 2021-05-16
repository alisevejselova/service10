package com.example.service10;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.service10.restarter.ServiceRestartBroadcastReceiver;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.i("MainActivity","onCreate");

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity"," onResume");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            // nad 21
            ServiceRestartBroadcastReceiver.scheduleJob(getApplicationContext());
        } else {
            ProcessMainClass bck = new ProcessMainClass();
            bck.launchService(getApplicationContext());
        }
       finish();
    }



}