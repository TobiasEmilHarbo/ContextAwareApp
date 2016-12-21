package com.example.tobias.contextawareapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by rasmus on 17/12/2016.
 */

public class ActivityWidget implements Widget{

    private Activity activity;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private SensorEventListener eventListener;

    private boolean queueHasReachedSampleSize = false;

    private int windowSampleSize = 128;

    final private LinkedBlockingDeque<Float[]> activityLog = new LinkedBlockingDeque<>(windowSampleSize * 2);
    private double gravity = 9.816;
    private List<Double[]> windowResults = new ArrayList<>();

    private NewWindowResultListener onNewWindowResultCallback;

    public ActivityWidget(Activity activity) {

        this.activity = activity;

        sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        eventListener = new SensorEventListener() {

            private int samplesSinceLastWindow = 0;

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                if (sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                    return;

                float sensorX = Math.abs(sensorEvent.values[0]);
                float sensorY = Math.abs(sensorEvent.values[1]);
                float sensorZ = Math.abs(sensorEvent.values[2]);

                //Log.d(DEBUG_TAG, "ACCELEROMETER CHANGED: X: " + sensorX + " Y: " + sensorY + " Z: " + sensorZ);

                logData(sensorX, sensorY, sensorZ);

                queueHasReachedSampleSize = (activityLog.size() > windowSampleSize);

                //if(queueHasReachedSampleSize)
                //{
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

                //Log.d(DEBUG_TAG, "activityLog.size() "+activityLog.size());
                //}
                //else if(activityLog.size() < 2)
                //{
                //    Toast.makeText(getContext().getApplicationContext(), "Filling array with data...", Toast.LENGTH_LONG).show();
                //}
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }

    private Context getContext(){
        return activity.getApplicationContext();
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

            //Log.d(DEBUG_TAG, x + " : " + y + " : " + z);

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

        //Log.d(DEBUG_TAG, "min: " + min + " | max: " + max + " | standard deviation: " + standardDeviation );

        windowResults.add(new Double[]{
                min,
                max,
                standardDeviation
        });

        Log.d(DEBUG_TAG, "-------- New entry in window results array --------");

        try {
            onNewWindowResultCallback.onNewResult();
        } catch (NullPointerException e) {
            //ignore
        }
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

    public void startDataGathering(NewWindowResultListener onNewWindowResultCallback) {
        this.onNewWindowResultCallback = onNewWindowResultCallback;
        this.startDataGathering();
    }

    public void startDataGathering() {
        try {
            sensorManager.unregisterListener(eventListener);
        }catch (NullPointerException e) {} //ignore

        sensorManager.registerListener(eventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void clearData() {
        activityLog.clear();
        windowResults.clear();
        Toast.makeText(getContext(), "Array was cleared.", Toast.LENGTH_SHORT).show();
    }

    public Double[] getNewestWindowResult() throws IndexOutOfBoundsException {
        try
        {
            return windowResults.get(windowResults.size() - 1);
        }
        catch (IndexOutOfBoundsException e)
        {
            Log.d(DEBUG_TAG, "-------- NO ENTRIES HAVE BEEN STORED JET --------");
            throw new IndexOutOfBoundsException(e.getMessage());
        }
    }
}
