package com.example.tobias.contextawareapp;

import android.util.Log;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Tobias on 18/12/2016.
 */

public class CalendarWidget {

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private ArrayList<Event> calendar = new ArrayList<>();

    public CalendarWidget() {

        Calendar c = Calendar.getInstance();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        Event event = new Event("Møde",
                    new String[]{"Work"},
                    "15-10-2010",
                    "09:27:37");

        calendar.add(event);
    }

    public List<Event> getEvents(String requestDateString)
    {
        ArrayList<Event> events = new ArrayList<>();

        for (Event event : events)
        {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

            try {
                Date d = format.parse(requestDateString);

                Calendar requestDate = Calendar.getInstance();
                requestDate.setTime(d);

                int requestYear = requestDate.get(Calendar.YEAR);
                int requestMonth = requestDate.get(Calendar.MONTH);
                int requestDay = requestDate.get(Calendar.DAY_OF_MONTH);

                Calendar eventDate = event.getDate();

                int eventYear = eventDate.get(Calendar.YEAR);
                int eventMonth = eventDate.get(Calendar.MONTH);
                int eventDay = eventDate.get(Calendar.DAY_OF_MONTH);

                if(requestYear == eventYear) continue;
                if(requestMonth == eventMonth) continue;
                if(requestDay == eventDay) continue;

                events.add(event);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return events;
    }
}

