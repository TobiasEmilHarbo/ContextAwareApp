package com.example.tobias.contextawareapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

        Button writeDataToFileBtn = (Button) findViewById(R.id.write_data_to_file_btn);
        Button startLoggingBtn = (Button) findViewById(R.id.start_loggin_btn);
        Button pauseLoggingBtn = (Button) findViewById(R.id.pause_logging_btn);
        Button clearDataBtn = (Button) findViewById(R.id.clear_data_btn);

        final EditText fileNameTxt = (EditText) findViewById(R.id.file_name_txt);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        final File fileDir = this.getExternalFilesDir(null);

        writeDataToFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!fileNameTxt.getText().toString().matches(""))
                {
                    String fileName = fileNameTxt.getText().toString() + ".csv";

                    try {
                        File file = new File(fileDir, fileName);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

                        for (Double[] result : windowsResults) {
                            String log = result[0] + ", " + result[1] + ", " + result[2];
                            writer.write(log);
                            writer.write("\r\n");
                        }

                        writer.close();
                        MediaScannerConnection.scanFile(getApplicationContext(),
                                new String[]{file.toString()},
                                null,
                                null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(), windowsResults.size() + " records was written to " + fileName, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "File name missing.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        startLoggingBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  try {
                      sensorManager.unregisterListener(eventListener);
                  }catch (NullPointerException e) {} //ignore

                  sensorManager.registerListener(eventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

                  Toast.makeText(getApplicationContext(), "Logging was started.", Toast.LENGTH_SHORT).show();
              }
          });

        pauseLoggingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sensorManager.unregisterListener(eventListener);
                }catch (NullPointerException e) {} //ignore
                Toast.makeText(getApplicationContext(), "Logging was paused.", Toast.LENGTH_SHORT).show();
            }
        });

        clearDataBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  activityLog.clear();
                  windowsResults.clear();
                  Toast.makeText(getApplicationContext(), "Array was cleared.", Toast.LENGTH_SHORT).show();
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
                                Looper.prepare();

                                showToast();
                                startWindow();
                            }
                        }).start();
                    }
                }
                else if(activityLog.size() < 2)
                {
                    Toast.makeText(getApplicationContext(), "Filling array with data...", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private void showToast() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Start analysing data of sliding window.", Toast.LENGTH_LONG).show();

            }
        });
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

        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Data from sliding window was logged.", Toast.LENGTH_SHORT).show();
            }
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
    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            wakeLock.release();
        }catch (NullPointerException e) {} //ignore
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
