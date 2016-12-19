package com.example.tobias.contextawareapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
/*
            locationWidget.startDataGathering();

            activityWidget.startDataGathering(new OnNewWindowResultListener() {
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
*/
        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "PAUSE");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                long[] pattern = {0, 400, 100, 100};

                NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(android.R.drawable.ic_popup_reminder)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!").setVibrate(pattern);

                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);

                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);

                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // mId allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());

            }
        }, 5000);
    }
}
