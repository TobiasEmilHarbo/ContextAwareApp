package com.example.tobias.contextawareapp;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by Tobias on 17/12/2016.
 */

public class LocationActivityInterpreter implements Interpreter {

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private final Classifier cls;

    private HashMap<String, Widget> widgets = new HashMap<>();

    public LocationActivityInterpreter(InputStream model) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(model);
        cls = (Classifier) ois.readObject();
        ois.close();
    }

    public double interpret(String[] classes, String[] attributesArray, Double[] data) throws Exception {
        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(classes.length);

        for (int i = 0; i < classes.length; i++)
        {
            fvClassVal.addElement(classes[i]);
        }

        Attribute ClassAttribute = new Attribute("qt", fvClassVal);

        // Declare the feature vector
        FastVector attributes = new FastVector(attributesArray.length + 1);

        for (int i = 0; i < attributesArray.length; i++)
        {
            Attribute attr = new Attribute(attributesArray[i]);
            attributes.addElement(attr);
        }

        attributes.addElement(ClassAttribute);

        // Create empty instance
        Instances newDataEntry = new Instances("Rel", attributes, 1);
        newDataEntry.setClassIndex(newDataEntry.numAttributes() - 1);

        //Our instance
        Instance dataInstance = new DenseInstance(newDataEntry.numAttributes());
        newDataEntry.add(dataInstance);

        for (int i = 0; i < data.length; i++)
        {
            dataInstance.setValue((Attribute)attributes.elementAt(i), data[i]);
        }

        dataInstance.setDataset(newDataEntry);
        // deserialize model

        return cls.classifyInstance(dataInstance);
    }

    public double interpret() throws Exception
    {
        Widget activityWidget = widgets.get("activity");
        Widget locationWidget = widgets.get("location");

        double min = activityWidget.getNewestWindowResult()[0];
        double max = activityWidget.getNewestWindowResult()[1];
        double std = activityWidget.getNewestWindowResult()[1];

        double avg = locationWidget.getNewestWindowResult()[0];

        Log.d(DEBUG_TAG, "min: "+ min + " max: "+ max + " avg: " + avg);

        double classss = interpret(
                LocationActivityCalendarAggregator.locationActivityContexts.toArray(new String[0]),
                new String[]{
                        "min",
                        "max",
                        "avg_dist"
                }, new Double[]{
                        min,
                        max,
                        avg
                });

        return classss;
    }

    public void addWidget(String key, Widget widget)
    {
        widgets.put(key, widget);
    }
}
