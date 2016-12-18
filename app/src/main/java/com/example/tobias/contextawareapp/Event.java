package com.example.tobias.contextawareapp;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Tobias on 18/12/2016.
 */

public class Event {

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

    public String title()
    {
        return this.title;
    }
}
