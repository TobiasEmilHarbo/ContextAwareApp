package com.example.tobias.contextawareapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private SensorEventListener eventListener;

    private boolean queueHasReachedSampleSize = false;

    private int windowSampleSize = 128;
    private int sampleWindowOverlap = windowSampleSize / 2;
    final private LinkedBlockingDeque<String[]> activityLog = new LinkedBlockingDeque<>(windowSampleSize * 2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
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

                                startNewWindow();
                            }
                        }).start();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private void startNewWindow()
    {
        ArrayList<String[]> windowSamples = new ArrayList<>();

        int samplesSoFar = 0;

        for (String[] log : activityLog)
        {
            if(samplesSoFar >= windowSampleSize) break;

            samplesSoFar++;

            windowSamples.add(0, log);

            Log.d(DEBUG_TAG, log[1] + " : " + log[2] + " : " + log[3]);
        }

        // do calculation
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(eventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            sensorManager.unregisterListener(eventListener);
        }catch (NullPointerException e) {} //ignore
    }

    private void logData(float x, float y, float z)
    {
        Long timeStamp = System.currentTimeMillis();

        String[] log = new String[]{
                Long.toString(timeStamp),
                Float.toString(x),
                Float.toString(y),
                Float.toString(z)
        };

        while(!activityLog.offerFirst(log))
        {
            activityLog.pollLast();
        }
    }
}
