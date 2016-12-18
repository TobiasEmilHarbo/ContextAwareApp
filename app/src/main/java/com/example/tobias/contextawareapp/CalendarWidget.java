package com.example.tobias.contextawareapp;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Tobias on 18/12/2016.
 */

public class CalendarWidget {

    final private String DEBUG_TAG = this.getClass().getSimpleName();

    private ArrayList<Event> calendar = new ArrayList<>();

    public CalendarWidget()
    {
        Event event = new Event("Møde",
                new String[]{"Work"},
                "15-10-2016",
                "09:27:37");

        Event event2 = new Event("Shopping",
                new String[]{"Family"},
                "20-10-2016",
                "09:27:37");

        Event event3 = new Event("Lecture",
                new String[]{"Work"},
                "15-10-2016",
                "12:27:37");

        calendar.add(event);
        calendar.add(event2);
        calendar.add(event3);
    }

    public List<Event> getEvents()
    {
        Collections.sort(calendar);
        return calendar;
    }

    public List<Event> getEventsOfDate(String requestDateString)
    {
        ArrayList<Event> events = new ArrayList<>();

        for (Event event : calendar)
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

                if(requestYear != eventYear) continue;
                if(requestMonth != eventMonth) continue;
                if(requestDay != eventDay) continue;

                events.add(event);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return events;
    }

    public List<Event> getEventsWithTagsOfDate(String[] tags, String date)
    {
        List<Event> events = getEventsOfDate(date);

        for(Iterator<Event> iter = events.iterator(); iter.hasNext();)
        {
            Event event = iter.next();

            if(!event.isTaggedWith(tags))
            {
                iter.remove();
            }
        }

        return events;
    }
}

