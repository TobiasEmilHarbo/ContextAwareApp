package com.example.tobias.contextawareapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import static android.app.Notification.PRIORITY_HIGH;

public class MainActivity extends AppCompatActivity {

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    LocationActivityCalendarAggregator aggregator;
    private Database database = new Database();
    ArrayAdapter<String> reminderAdapter;
    ArrayAdapter<String> eventAdapter;

    private ArrayList<String> reminderList = new ArrayList<>();
    private ArrayList<String> eventList = new ArrayList<>();
    private CalendarWidget calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar = new CalendarWidget();
        aggregator = new LocationActivityCalendarAggregator(this);

        this.setEventList(new ArrayList<String>(){{add("No events found for this context");}});
        this.setReminderList(new ArrayList<String>(){{add("No reminders found for this context");}});

        eventAdapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, eventList);
        ListView eventListView = (ListView) findViewById(R.id.event_list);
        eventListView.setAdapter(eventAdapter);

        reminderAdapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, reminderList);

        ListView reminderListView = (ListView) findViewById(R.id.reminder_list);
        reminderListView.setAdapter(reminderAdapter);

        try {

            aggregator.addContextListener(new ContextListener(){

                @Override
                public void onContextChange(Double context) {

                    Log.d(DEBUG_TAG, "CONTEXT: " + context);

                    if (context == LocationActivityCalendarAggregator.locationActivityContexts.indexOf(LocationActivityCalendarAggregator.CYCLING_NEAR)
                            || context == LocationActivityCalendarAggregator.locationActivityContexts.indexOf(LocationActivityCalendarAggregator.WALKING_NEAR)) {
                        List<Event> events = calendar.getEventsWithTagsOfDate(new String[]{"Work"}, "19-12-2016");
                        List<HashMap<String, String>> reminders = database.getReminders("tag", "work"); //tag == work

                        ArrayList<String> eventDisplayStrings = new ArrayList<>();
                        ArrayList<String> reminderDisplayStrings = new ArrayList<>();

                        eventList.clear();
                        reminderList.clear();

                        for (Event event : events) {
                            eventDisplayStrings.add(event.getTitle() + " | Starts at: "  + event.getStartTime());
                            eventList.add(event.getTitle() + " | Starts at: " + event.getStartTime());
                        }

                        for (HashMap<String, String> reminder : reminders) {
                            reminderDisplayStrings.add(reminder.get("title") + ": " + reminder.get("content"));
                            reminderList.add(reminder.get("title") + ": " + reminder.get("content"));
                        }

                        setEventList(eventDisplayStrings);
                        setReminderList(reminderDisplayStrings);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateListViews();
                            }
                        });

                        int reminderCount = events.size() + reminders.size();

                        long[] pattern = {0, 400, 100, 100};
                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(MainActivity.this)
                                        .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                                        .setContentTitle("You are approaching work")
                                        .setContentText("You have " + reminderCount + " reminders")
                                        .setPriority(PRIORITY_HIGH)
                                        .setVibrate(pattern)
                                        .setSound(alarmSound);

                        // Creates an explicit intent for an Activity in your app
                        Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);

                        resultIntent.putExtra("context", context);

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

                }
            });

            aggregator.startMonitoringContext();

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
    }

    public void setEventList(ArrayList<String> list)
    {
        this.eventList = list;
    }

    public void setReminderList(ArrayList<String> list)
    {
        this.reminderList = list;
    }

    public void updateListViews()
    {
        reminderAdapter.clear();
        reminderAdapter.addAll(reminderList);
        reminderAdapter.notifyDataSetChanged();

        eventAdapter.clear();
        eventAdapter.addAll(eventList);
        eventAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        double context = intent.getDoubleExtra("context", 999.0);

        if (context == LocationActivityCalendarAggregator.locationActivityContexts.indexOf(LocationActivityCalendarAggregator.CYCLING_NEAR)
            || context == LocationActivityCalendarAggregator.locationActivityContexts.indexOf(LocationActivityCalendarAggregator.WALKING_NEAR)) {

           getReminders();
        }
    }

    public void getReminders()
    {
        List<Event> events = calendar.getEventsWithTagsOfDate(new String[]{"Work"}, "19-12-2016");
        List<HashMap<String, String>> reminders = database.getReminders("tag", "work"); //tag == work

        ArrayList<String> eventDisplayStrings = new ArrayList<>();
        ArrayList<String> reminderDisplayStrings = new ArrayList<>();

        eventList.clear();
        reminderList.clear();

        for (Event event : events) {
            eventDisplayStrings.add(event.getTitle() + " | Starts at: " + event.getStart().getTime());
            eventList.add(event.getTitle() + " | Starts at: " + event.getStartTime());
        }

        for (HashMap<String, String> reminder : reminders) {
            reminderDisplayStrings.add(reminder.get("title") + ": " + reminder.get("content"));
            reminderList.add(reminder.get("title") + ": " + reminder.get("content"));
        }

        setEventList(eventDisplayStrings);
        setReminderList(reminderDisplayStrings);
        updateListViews();
    }
}
