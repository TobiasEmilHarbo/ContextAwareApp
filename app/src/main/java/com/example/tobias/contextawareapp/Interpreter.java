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

    static public double interpret(InputStream model, double min, double max) throws Exception
    {
        Attribute minMagAttr = new Attribute("min");
        Attribute maxMagAttr = new Attribute("max");

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(2);

        fvClassVal.addElement("cycling");
        fvClassVal.addElement("walk");
        Attribute ClassAttribute = new Attribute("qt", fvClassVal);

        // Declare the feature vector
        FastVector attributes = new FastVector(3);

        attributes.addElement(minMagAttr);
        attributes.addElement(maxMagAttr);
        attributes.addElement(ClassAttribute);

        // Create empty instance
        Instances newDataEntry = new Instances("Rel", attributes, 7);
        newDataEntry.setClassIndex(newDataEntry.numAttributes() - 1);

        //Our instance
        Instance dataInstance = new DenseInstance(newDataEntry.numAttributes());
        newDataEntry.add(dataInstance);

        dataInstance.setValue((Attribute)attributes.elementAt(0), min);
        dataInstance.setValue((Attribute)attributes.elementAt(1), max);

        dataInstance.setDataset(newDataEntry);
        // deserialize model
        ObjectInputStream ois = new ObjectInputStream(model
                );
        Classifier cls = (Classifier) ois.readObject();
        ois.close();
        return cls.classifyInstance(dataInstance);
    }
}
