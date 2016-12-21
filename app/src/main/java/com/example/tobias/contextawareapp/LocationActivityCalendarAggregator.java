package com.example.tobias.contextawareapp;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by rasmus on 18/12/2016.
 */

public class LocationActivityCalendarAggregator implements Aggregator {

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private final LocationWidget locationWidget;
    private final ActivityWidget activityWidget;
    private Interpreter interpreter;

    final static String WALKING_NEAR = "walking_near";
    final static String WALKING_FAR = "walking_far";
    final static String CYCLING_NEAR = "cycling_near";
    final static String CYCLING_FAR = "cycling_far";

    public static ArrayList<String> locationActivityContexts = new ArrayList<String>() {{
        add(WALKING_NEAR);
        add(WALKING_FAR);
        add(CYCLING_NEAR);
        add(CYCLING_FAR);
    }};

    int contextLogLength = 1;
    final private Deque<Double> locationActivityContextLog = new LinkedBlockingDeque<>(contextLogLength);


    private ContextListener contextListener;

    public LocationActivityCalendarAggregator(Activity activity) {

        locationWidget = new LocationWidget(activity);
        activityWidget = new ActivityWidget(activity);

        InputStream model = null;
        try {
            model = activity.getAssets().open("ActivityAndDistance_NaiveBayes.model");

            interpreter = new LocationActivityInterpreter(model);

            interpreter.addWidget("activity", activityWidget);
            interpreter.addWidget("location", locationWidget);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addContextListener(ContextListener contextListener) {

        this.contextListener = contextListener;
    }

    public void startMonitoringContext() {
        locationWidget.startDataGathering();

        activityWidget.startDataGathering(new NewWindowResultListener() {
            private double lastKnownContext = -0.0;

            @Override
            public void onNewResult() {

                try
                {
                    //Calendar dateTime = Calendar.getInstance();

                    Double currentContext = interpreter.interpret();


                    while(!locationActivityContextLog.offerFirst(currentContext))
                    {
                        locationActivityContextLog.pollLast();
                    }

                    Boolean contextIsCertain = false;
                    for (Double context : locationActivityContextLog)
                    {
                        if(context.equals(currentContext)) contextIsCertain = true;
                    }

                    //if the same context has been detected x times in a row
                    if(contextIsCertain)
                    {
                        Log.d(DEBUG_TAG, "----------------------------------- CONTEXT DETECTED " + locationActivityContexts.get(currentContext.intValue()));
                        if(lastKnownContext != currentContext)
                        {
                            Log.d(DEBUG_TAG, "----------------------------------- CONTEXT CHANGED " + locationActivityContexts.get(currentContext.intValue()));
                            lastKnownContext = currentContext;
                            contextListener.onContextChange(currentContext);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    //Log.d(DEBUG_TAG, e.getMessage());
                }
            }
        });
    }
}
