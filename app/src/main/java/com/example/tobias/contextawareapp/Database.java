package com.example.tobias.contextawareapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tobias on 19/12/2016.
 */

public class Database {

    private List<HashMap<String, String>> reminderTable = new ArrayList<>();

    public Database()
    {
        reminderTable.add(new HashMap<String, String>() {{
            put("title", "Reminder to self");
            put("content", "Talk to Jeff about butt holes");
            put("tag",   "work");
        }});
    }

    public List getReminders()
    {
        return reminderTable;
    }

    public List getReminders(String attr, String value)
    {
        List<HashMap<String, String>> reminders = new ArrayList<>();

        for(HashMap<String, String> reminder : reminderTable)
        {
            if(reminder.get(attr).equals(value)) reminders.add(reminder);
        }

        return reminders;
    }
}
