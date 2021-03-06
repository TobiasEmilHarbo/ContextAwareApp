package com.example.tobias.contextawareapp;

import android.media.MediaScannerConnection;
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

public class OfflineDataGatheringActivity extends AppCompatActivity {

    final private String DEBUG_TAG = this.getClass().getSimpleName();

    private List<Double> windowResults;
    private PowerManager.WakeLock wakeLock;

    private List<Double[]> activityAndLocationData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_data_gathering);

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

        final ActivityWidget activityWidget = new ActivityWidget(getApplicationContext());

        final LocationWidget locationWidget = new LocationWidget(this);

        writeDataToFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!fileNameTxt.getText().toString().matches(""))
                {

                    String fileName = fileNameTxt.getText().toString() + ".csv";

                    try {
                        Log.d(DEBUG_TAG, "Started writing to file");

                        File file = new File(fileDir, fileName);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

                        for (Double[] result : activityAndLocationData) {
                            String log = result[0] + ", " + result[1] + ", " + result[2] + ", " + result[3];
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

                    Log.d(DEBUG_TAG, "Finished writing to file");
                    Toast.makeText(getApplicationContext(), activityAndLocationData.size() + " records was written to " + fileName, Toast.LENGTH_SHORT).show();
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
                activityWidget.startDataGathering(new OnNewWindowResultListener() {
                    @Override
                    public void onNewResult() {

                        try {

                            Toast.makeText(getApplicationContext(), "Logging Started", Toast.LENGTH_SHORT).show();
                            Double[] newestActivityData = activityWidget.getNewestWindowResult();
                            Double[] newestLocationData = locationWidget.getNewestWindowResult();

                            activityAndLocationData.add(new Double[]{
                                    newestActivityData[0],
                                    newestActivityData[1],
                                    newestActivityData[2],
                                    newestLocationData[0]
                            });
                        }catch (ArrayIndexOutOfBoundsException e){
                            //Ignore
                        }
                    }
                });

                locationWidget.startDataGathering();
            }
        });

        pauseLoggingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //activityWidget.pauseDatagathering();
            }
        });

        clearDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityWidget.clearData();
            }
        });

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
}
