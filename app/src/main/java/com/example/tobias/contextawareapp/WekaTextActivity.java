package com.example.tobias.contextawareapp;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

public class WekaTextActivity extends AppCompatActivity {
    final private String DEBUG_TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weka_test);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {

            Attribute minMagAttr = new Attribute("min_mag");
            Attribute maxMagAttr = new Attribute("max_mag");

            // Declare the class attribute along with its values
            FastVector fvClassVal = new FastVector(2);

            fvClassVal.addElement("cycling");
            fvClassVal.addElement("walk");
            Attribute ClassAttribute = new Attribute("qt", fvClassVal);

            // Declare the feature vector
            FastVector attributes = new FastVector(7);

            attributes.addElement(minMagAttr);
            attributes.addElement(maxMagAttr);
            attributes.addElement(ClassAttribute);

            // Create empty instance
            Instances newDataEntry = new Instances("Rel", attributes, 7);
            newDataEntry.setClassIndex(newDataEntry.numAttributes() - 1);

            //Our instance
            Instance dataInstance = new DenseInstance(newDataEntry.numAttributes());
            newDataEntry.add(dataInstance);
            double min = -4.96257917;
            double max = 3.747140604;

            dataInstance.setValue((Attribute)attributes.elementAt(0), min);
            dataInstance.setValue((Attribute)attributes.elementAt(1), max);

            dataInstance.setMissing(2); //set which attribute is missing from the new data set (class)
            dataInstance.setDataset(newDataEntry);

            // deserialize model
            ObjectInputStream ois = new ObjectInputStream(
                    getAssets().open("J48_walking_cycling_NEW.model"));
            Classifier cls = (Classifier) ois.readObject();
            ois.close();
            double fDistribution = cls.classifyInstance(dataInstance);
            Log.d("WEKA", fDistribution + "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
