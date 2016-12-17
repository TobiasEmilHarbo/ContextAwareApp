package com.example.tobias.contextawareapp;

import java.io.InputStream;
import java.io.ObjectInputStream;

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

    private InputStream model;

    public Interpreter(InputStream model)
    {
        this.model = model;
    }

    public double interpret(String[] classes, String[] attributesArray, Double[] data) throws Exception
    {
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
        ObjectInputStream ois = new ObjectInputStream(this.model);

        Classifier cls = (Classifier) ois.readObject();
        ois.close();
        return cls.classifyInstance(dataInstance);
    }
}
