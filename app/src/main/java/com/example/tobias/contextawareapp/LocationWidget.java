package com.example.tobias.contextawareapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by rasmus on 17/12/2016.
 */

public class LocationWidget implements LocationListener{

    final static String locationProvider = LocationManager.GPS_PROVIDER;

    private Context context;

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private SensorEventListener eventListener;

    private boolean queueHasReachedSampleSize = false;

    private int windowSampleSize = 128;

    final private LinkedBlockingDeque<Float[]> activityLog = new LinkedBlockingDeque<>(windowSampleSize * 2);
    private double gravity = 9.816;
    private List<Double[]> windowsResults = new ArrayList<>();

    public LocationWidget(Context context) {

        this.context = context;

        eventListener = new SensorEventListener() {

            private int samplesSinceLastWindow = 0;

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                if (sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                    return;

                float sensorX = Math.abs(sensorEvent.values[0]);
                float sensorY = Math.abs(sensorEvent.values[1]);
                float sensorZ = Math.abs(sensorEvent.values[2]);

                logData(sensorX, sensorY, sensorZ);

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
                    Toast.makeText(getContext().getApplicationContext(), "Filling array with data...", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }

    private Context getContext(){
        return context;
    }

    private void logData(float x, float y, float z)
    {
        Float[] log = new Float[]{x, y, z};

        while(!activityLog.offerFirst(log))
        {
            activityLog.pollLast();
        }
    }

    private void startWindow()
    {
        Double max = null;
        Double min = null;
        double norm = 0;
        double totalNorm = 0.0;

        ArrayList<Float[]> windowSamples = new ArrayList<>();
        ArrayList<Double> euclideanNorms = new ArrayList<>();

        int samplesSoFar = 0;

        for (Float[] log : activityLog)
        {
            if(samplesSoFar >= windowSampleSize) break;

            samplesSoFar++;

            windowSamples.add(0, log);

            float x = log[0];
            float y = log[1];
            float z = log[2];

            Log.d(DEBUG_TAG, x + " : " + y + " : " + z);

            norm = calcEuclideanNorm(x, y, z);

            euclideanNorms.add(norm);

            totalNorm += norm;

            if(max == null
                    || max < norm)
            {
                max = norm;
            }

            if(min == null
                    || min > norm)
            {
                min = norm;
            }
        }

        double standardDeviation = calcStandardDeviation(euclideanNorms, (totalNorm / windowSampleSize));

        Log.d(DEBUG_TAG, "min: " + min + " | max: " + max + " | standard deviation: " + standardDeviation );

        windowsResults.add(new Double[]{
                min,
                max,
                standardDeviation
        });
    }

    private double calcEuclideanNorm(float x, float y, float z)
    {
        return Math.sqrt( (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))) - gravity;
    }

    private double calcStandardDeviation(ArrayList<Double> euclideanNorms, double avg)
    {
        double deviation = 0;

        for (Double norm : euclideanNorms)
        {
            deviation =+ Math.pow((norm - avg), 2);
        }

        double variance = deviation / windowSampleSize;

        return Math.sqrt(variance);
    }

    public void startDatagathering() {

    }

    public void pauseDatagathering() {

    }

    public void clearData() {
        activityLog.clear();
        windowsResults.clear();
        Toast.makeText(getContext(), "Array was cleared.", Toast.LENGTH_SHORT).show();
    }

    public void requestLocationUpdates(LocationManager locationManager, Activity activity, int time){

        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(locationProvider, time, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {

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
