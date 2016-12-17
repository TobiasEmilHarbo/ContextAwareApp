package com.example.tobias.contextawareapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by rasmus on 17/12/2016.
 */

public class LocationWidget{
    private LocationListener locationListener;
    private LocationManager locationManager;

    private Activity activity;

    final private String DEBUG_TAG = this.getClass().getSimpleName();

    private boolean queueHasReachedSampleSize = false;
    private int windowSampleSize = 10;
    final private LinkedBlockingDeque<Float[]> activityLog = new LinkedBlockingDeque<>(windowSampleSize * 2);
    private List<Double[]> windowsResults = new ArrayList<>();

    public LocationWidget(Activity activity) {
        this.activity = activity;
    }

    private void startWindow()
    {
        double averageLongitude = 0.0;
        double averageLatitude = 0.0;

        for (Float[] log : activityLog) {
            averageLongitude += log[0];
            averageLatitude += log[1];
        }

        Log.d(DEBUG_TAG, "AvgLong:" + averageLongitude + " | AvgLat: " + averageLatitude );

        windowsResults.add(new Double[]{
                averageLongitude,
                averageLatitude
        });
    }

    public void startDatagathering() {
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            int samplesSinceLastWindow = 0;

            public void onLocationChanged(Location location){
                Log.d(DEBUG_TAG, "LOCATION CHANGED: Long: " + location.getLongitude() + " Lat: " + location.getLatitude());

                logData( (float)location.getLongitude(),(float)location.getLatitude());

                queueHasReachedSampleSize = (activityLog.size() > windowSampleSize);

                if(queueHasReachedSampleSize)
                {
                    samplesSinceLastWindow++;

                    if(samplesSinceLastWindow > windowSampleSize / 2)
                    {
                        samplesSinceLastWindow = 0;

                        new Thread(new Runnable() {
                            public void run(){
                                Looper.prepare();

                                startWindow();
                            }
                        }).start();
                    }
                }
                else if(activityLog.size() < 2)
                {
                    Toast.makeText(activity.getApplicationContext(), "Filling array with data...", Toast.LENGTH_LONG).show();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(DEBUG_TAG, "STATUS CHANGED");
            }

            public void onProviderEnabled(String provider) {
                Log.d(DEBUG_TAG, "ENABLED");
            }

            public void onProviderDisabled(String provider) {
                Log.d(DEBUG_TAG, "DISABLED");
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

    }

    public void pauseDatagathering() {

    }

    public void clearData() {
        activityLog.clear();
        windowsResults.clear();
        Toast.makeText(activity.getApplicationContext(), "Array was cleared.", Toast.LENGTH_SHORT).show();
    }

    private void logData(float x, float y)
    {
        Float[] log = new Float[]{x, y};

        while(!activityLog.offerFirst(log))
        {
            activityLog.pollLast();
        }
    }
}
