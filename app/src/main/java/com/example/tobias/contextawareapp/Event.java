package com.example.tobias.contextawareapp;

import android.util.Log;

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
    private String title;
    private Calendar date;
    private String[] tags;

    public Event(String title, String[] tags, String date, String time)
    {
        this.title = title;
        this.tags = tags;

        try
        {
            String dateStamp = date+"T"+time;
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");

            Date d = format.parse(dateStamp);

            this.date = Calendar.getInstance();
            this.date.setTime(d);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public Calendar getDate()
    {
        return this.date;
    }

    public String[] getTags()
    {
        return this.tags;
    }

    public String getTitle()
    {
        return this.title;
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

        Calendar c2 = other.getDate();
        Calendar c1 = this.getDate();

        return c1.compareTo(c2);
    }
}
