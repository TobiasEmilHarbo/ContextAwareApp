package com.example.tobias.contextawareapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private LocationWidget locationWidget;
    private ActivityWidget activityWidget;
    private Interpreter activityAndLocationInterpreter;
    final Aggregator aggregator = new Aggregator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            locationWidget = new LocationWidget(this);
            activityWidget = new ActivityWidget(this);

            InputStream model = getAssets().open("ActivityAndDistance_J48.model");
            activityAndLocationInterpreter = new Interpreter(model);

            activityAndLocationInterpreter.addWidget(activityWidget); //index 0
            activityAndLocationInterpreter.addWidget(locationWidget); //index 1

            final TextView viewById = (TextView) findViewById(R.id.logView);

            aggregator.addInterpreter(activityAndLocationInterpreter);

            locationWidget.startDataGathering();

            activityWidget.startDataGathering(new OnNewWindowResultCallback() {
                @Override
                public void onNewResult() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                Calendar dateTime = Calendar.getInstance();

                                Double classs = aggregator.getReminders();

                                Log.d(DEBUG_TAG, "CLASS: " + classs);

                                String logString = viewById.getText().toString();
                                logString = System.getProperty ("line.separator") + Interpreter.classes[classs.intValue()] + "    |     " + dateTime.get(Calendar.HOUR_OF_DAY) + ":" + dateTime.get(Calendar.MINUTE) + ":" + dateTime.get(Calendar.SECOND) + logString;
                                viewById.setText(logString);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.d(DEBUG_TAG, e.getMessage());
                            }
                        }
                    });

                }
            });

        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
