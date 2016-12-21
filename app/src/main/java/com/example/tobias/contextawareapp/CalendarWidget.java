package com.example.tobias.contextawareapp;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tobias on 18/12/2016.
 */

public class CalendarWidget {

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    static SimpleDateFormat dateTimeFirmat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");

    private ArrayList<Event> calendar = new ArrayList<>();

    public CalendarWidget()
    {
        Event event = new Event("Meeting",
                new String[]{"Work"},
                "19-12-2016",
                "13:15:00",
                "19-12-2016",
                "14:00:00");

        Event event2 = new Event("Shopping",
                new String[]{"Family"},
                "20-10-2016",
                "09:27:37",
                "20-10-2016",
                "12:27:37");

        Event event3 = new Event("Lecture",
                new String[]{"Work"},
                "19-12-2016",
                "09:00:00",
                "19-12-2016",
                "11:00:00");

        calendar.add(event);
        calendar.add(event2);
        calendar.add(event3);

    }

    public ArrayList<Event> getEvents()
    {
        Collections.sort(calendar);
        return calendar;
    }

    public List<Event> getEventsOfDate(String requestDateString)
    {
        Collections.sort(calendar);
        ArrayList<Event> events = new ArrayList<>();

        for (Event event : calendar)
        {
            try {
                Date d = dateFormat.parse(requestDateString);

                Calendar requestDate = Calendar.getInstance();
                requestDate.setTime(d);

                int requestYear = requestDate.get(Calendar.YEAR);
                int requestMonth = requestDate.get(Calendar.MONTH);
                int requestDay = requestDate.get(Calendar.DAY_OF_MONTH);

                Calendar eventDate = event.getStart();

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

    public List<Event> getEventsHappeningNow(String requestTime, String requestsDate)
    {
        ArrayList<Event> events = new ArrayList<>();

        for (Event event : calendar)
        {
            try {
                Date requestDateTime = dateTimeFirmat.parse(requestTime+"T"+requestsDate);

                Date eventStartDate = event.getStart().getTime();

                Date eventEndDate = event.getEnd().getTime();

                if(eventStartDate.before(requestDateTime)
                && eventEndDate.after(requestDateTime))
                {
                    events.add(event);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return events;
    }

    public List<Event> getEventsWithTagsOfDate(String[] tags, String date)
    {
        Collections.sort(calendar);
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

