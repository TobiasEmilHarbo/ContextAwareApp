package com.example.tobias.contextawareapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
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

    private Double workEasting;
    private Double workNorthing;
    private Deg2UTM converter;

    private LocationListener locationListener;
    private LocationManager locationManager;

    private Activity activity;

    final private String DEBUG_TAG = this.getClass().getSimpleName();

    private boolean queueHasReachedSampleSize = false;
    private int windowSampleSize = 32;
    final private LinkedBlockingDeque<Float[]> activityLog = new LinkedBlockingDeque<>(windowSampleSize * 2);
    private List<Double> windowsResults = new ArrayList<>();

    private NewWindowsResultsCallback callback;

    public LocationWidget(Activity activity) {
        this.activity = activity;
        converter = new Deg2UTM();

        converter.convert(56.172649, 10.189055);

        workEasting = converter.getEasting();
        workNorthing = converter.getNorthing();
    }

    public void startDatagathering(NewWindowsResultsCallback callback) {

        this.callback = callback;

        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            private int samplesSinceLastWindow = 0;

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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    private void startWindow()
    {
        double sumOfDistances = 0.0;
        double averageDistance;


        int samplesSoFar = 0;

        for (Float[] log : activityLog) {

            if(samplesSoFar >= windowSampleSize) break;
            samplesSoFar++;

            converter.convert(log[1], log[0]);
            double currentEasting = converter.getEasting();
            double currentNorthing = converter.getNorthing();

            double distBetweenWorkAndCurrent = calcEuclidianDist(workEasting, workNorthing, currentEasting, currentNorthing);

            sumOfDistances += distBetweenWorkAndCurrent;
            Log.d(DEBUG_TAG, "Dist: " + distBetweenWorkAndCurrent);
        }

        averageDistance = sumOfDistances/windowSampleSize;

        windowsResults.add(averageDistance);

        callback.calculated();

        Log.d(DEBUG_TAG, "Average: " + averageDistance);
    }

    private double calcEuclidianDist(double prevEasting, double prevNorthing, double currentEasting, double currentNorthing) {
        double sumOfX = 0;
        double sumOfY = 0;

        sumOfX += Math.pow(prevEasting - currentEasting, 2);
        sumOfY += Math.pow(prevNorthing - currentNorthing, 2);

        return Math.sqrt(sumOfX + sumOfY);
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

    public Double getNewestWindow() {
        return windowsResults.get(windowsResults.size() - 1);
    }
}
