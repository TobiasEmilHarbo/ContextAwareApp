package com.example.tobias.contextawareapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Tobias on 18/12/2016.
 */

public class Event implements Comparable{

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private final String startTime;
    private String title;
    private Calendar start;
    private Calendar end;

    private String[] tags;

    public Event(String title, String[] tags, String startDate, String startTime, String endDate, String endTime)
    {
        this.title = title;
        this.tags = tags;

        this.startTime = startTime;

        try
        {
            String startDateStamp = startDate+"T"+startTime;
            Date sd = CalendarWidget.dateTimeFirmat.parse(startDateStamp);

            this.start = Calendar.getInstance();
            this.start.setTime(sd);

            String endSateStamp = endDate+"T"+endTime;

            Date ed = CalendarWidget.dateTimeFirmat.parse(endSateStamp);

            this.end = Calendar.getInstance();
            this.end.setTime(ed);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public String getStartTime()
    {
        return this.startTime;
    }

    public Calendar getStart()
    {
        return start;
    }

    public Calendar getEnd() {
        return end;
    }

    public String[] getTags()
    {
        return tags;
    }

    public String getTitle()
    {
        return title;
    }

    public boolean isTaggedWith(String[] tags)
    {
        Boolean isTaggedWithAll = true;
        List<String> tagsList = Arrays.asList(tags);

        for(int i = 0; i < this.tags.length; i++)
        {
            if(!tagsList.contains(this.tags[i])) // if this tag of the event is not contained in the list of requested tags
            {
                isTaggedWithAll = false;
            }
        }

        return isTaggedWithAll;
    }

    @Override
    public int compareTo(Object o)
    {
        Event other = (Event) o;

        Calendar c2 = other.getStart();
        Calendar c1 = this.getStart();

        return c1.compareTo(c2);
    }
}
