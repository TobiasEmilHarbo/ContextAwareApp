package com.example.tobias.contextawareapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private SensorEventListener eventListener;

    private boolean queueHasReachedSampleSize = false;

    private int windowSampleSize = 128;

    private int sampleWindowOverlap = windowSampleSize / 2;

    final private LinkedBlockingDeque<Float[]> activityLog = new LinkedBlockingDeque<>(windowSampleSize * 2);
    private double gravity = 9.816;
    private List<Double[]> windowsResults = new ArrayList<>();
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.button);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        final File fileDir = this.getExternalFilesDir(null);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = "Log.csv";

                try {
                    File file = new File(fileDir, fileName);
                    if (!file.exists()){
                        file.createNewFile();
                    }

                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

                    for(Double[] result : windowsResults)
                    {
                        String log = result[0] + ", " + result[1] + ", " + result[2];
                        writer.write(log);
                        writer.write("\r\n");
                    }

                    writer.close();
                    MediaScannerConnection.scanFile(getApplicationContext(),
                            new String[] { file.toString() },
                            null,
                            null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

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

                                startWindow();

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

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(eventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            sensorManager.unregisterListener(eventListener);
        }catch (NullPointerException e) {} //ignore

        wakeLock.release();
    }

    private void logData(float x, float y, float z)
    {
        Long timeStamp = System.currentTimeMillis();

        Float[] log = new Float[]{x, y, z};

        while(!activityLog.offerFirst(log))
        {
            activityLog.pollLast();
        }
    }
}
