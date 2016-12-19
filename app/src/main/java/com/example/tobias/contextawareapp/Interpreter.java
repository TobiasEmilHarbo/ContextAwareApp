package com.example.tobias.contextawareapp;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by Tobias on 17/12/2016.
 */

public class Interpreter {

    final private String DEBUG_TAG = this.getClass().getSimpleName();
    private final Classifier cls;

    public static String[] classes = new String[]{
            "walking_near", //0.0
            "walking_far",  //1.0
            "cycling_near", //2.0
            "cycling_far",  //3.0
    };

    private List<Widget> widgets = new ArrayList<>();

    public Interpreter(InputStream model) throws IOException, ClassNotFoundException {
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
        Widget activityWidget = widgets.get(0);
        Widget locationWidget = widgets.get(1);

        double min = activityWidget.getNewestWindowResult()[0];
        double max = activityWidget.getNewestWindowResult()[1];
        double std = activityWidget.getNewestWindowResult()[1];

        double avg = locationWidget.getNewestWindowResult()[0];

        Log.d(DEBUG_TAG, "min: "+ min + " max: "+ max + " avg: " + avg);

        double classss = interpret(
                classes,
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

    public void addWidget(Widget widget)
    {
        widgets.add(widget);
    }
}
