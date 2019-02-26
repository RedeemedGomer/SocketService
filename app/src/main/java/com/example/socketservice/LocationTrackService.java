package com.example.socketservice;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by anupamchugh on 28/11/16.
 */

public class LocationTrackService extends Service implements LocationListener {

    boolean checkGPS = false;

    Context mContext;

    boolean checkNetwork = false;

    boolean canGetLocation = false;

    Location loc;
    double latitude;
    double longitude;



    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;


    private static final long MIN_TIME_BW_UPDATES = 1000;//1000 * 60 * 1;
    private LocationManager locationManager;

    public LocationTrackService(Context newContext) {
        Log.i("LTS_update", "LocationTrackService()");
        mContext = newContext;
        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);
        getLocation();
    }

    private Location getLocation() {
        Log.i("LTS_update", "getLocation()");
        try {

            if(locationManager != null) {
                // get GPS status
                checkGPS = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // get network provider status
                checkNetwork = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            }else{
                Log.d("LTS_debug", "locationManager was null");
            }
            if (!checkGPS && !checkNetwork) {
                Log.d("LTS.error", "No Service Provider is available");
            } else {
                this.canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services
                if (checkGPS) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.d("LT.error", "permissions not given");
                    }

                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (loc != null) {
                                latitude = loc.getLatitude();
                                longitude = loc.getLongitude();
                            }
                        }
                    }catch (SecurityException e){
                        e.printStackTrace();
//                        Log.e("LT.error", "permissions not given", e);
                    }

                }


            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return loc;
    }




    public double getLongitude() {
        if (loc != null) {
            longitude = loc.getLongitude();
        }
        return longitude;
    }

    public double getLatitude() {
        if (loc != null) {
            latitude = loc.getLatitude();
        }
        return latitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }




    public void stopListener() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationTrackService.this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("LTS", "onLocationChanged()");
        loc = location;
        if(loc!= null){
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}