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

    static public double interpret(InputStream model, String[] classes, String[] attributesArray, Double[] data) throws Exception
    {

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(classes.length);

        for (int i = 0; i < classes.length; i++)
        {
            fvClassVal.addElement(classes[i]);
        }

        Attribute ClassAttribute = new Attribute("qt", fvClassVal);

        // Declare the feature vector
        FastVector attributes = new FastVector(3);

    /*    Attribute minMagAttr = new Attribute("min");
        Attribute maxMagAttr = new Attribute("max");
        attributes.addElement(minMagAttr);
        attributes.addElement(maxMagAttr);
       */

        for (int i = 0; i < attributesArray.length; i++)
        {
            Attribute attr = new Attribute(attributesArray[i]);
            attributes.addElement(attr);
        }

        attributes.addElement(ClassAttribute);

        // Create empty instance
        Instances newDataEntry = new Instances("Rel", attributes, attributesArray.length + 1);
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
        ObjectInputStream ois = new ObjectInputStream(model
                );
        Classifier cls = (Classifier) ois.readObject();
        ois.close();
        return cls.classifyInstance(dataInstance);
    }
}
