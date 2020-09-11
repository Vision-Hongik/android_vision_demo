package com.example.vision;


import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;



public class MyGps {
    private LocationManager lm;
    private AppCompatActivity activity;
    private LocationListener gpsLocationListener;
    private String provider;

    //private AppCompatActivity act;
    public MyGps(AppCompatActivity activity, LocationListener locationListener) {
        this.activity = activity;
        lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        gpsLocationListener = locationListener;
    }

    public boolean startGps() {
        Log.e("t", "startGps: Start!!");

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this.activity.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
        else {
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            boolean flag = true;
            if (location == null)
            {
                Log.e("t", "startGps: 네트워크 없음!");
                flag = false;
            }
            else
            {
                Log.e("t", "startGps:" + location.getProvider());
                Log.e("t", "startGps:" + location.getLongitude());
                Log.e("t", "startGps:" + location.getLatitude());
                Log.e("t", "정확" + location.getAccuracy());
            }

            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    300,
                    0,
                    gpsLocationListener);
            return flag;
        }
        return false;
    }

}
