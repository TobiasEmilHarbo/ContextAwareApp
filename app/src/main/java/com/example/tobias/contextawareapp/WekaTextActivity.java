package com.example.tobias.contextawareapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ObjectInputStream;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
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

        try{
            // Declare two numeric attributes
            Attribute maxAttribute = new Attribute("max");
            Attribute minAttribute = new Attribute("min");

            // Declare the feature vector
            FastVector fvWekaAttributes = new FastVector(4);
            fvWekaAttributes.addElement(maxAttribute);
            fvWekaAttributes.addElement(minAttribute);

            Instances dataset = new Instances("Rel", fvWekaAttributes, 10);

            dataset.setClassIndex(1);

            // Create the instance
            Instance instance = new DenseInstance(2);
            instance.setValue((Attribute)fvWekaAttributes.elementAt(0), 26.37551941); //max
            instance.setValue((Attribute)fvWekaAttributes.elementAt(1), -28.489334331); //min

            // add the instance
            dataset.add(instance);

            Classifier cls;

            ObjectInputStream ois = new ObjectInputStream(
                    getAssets().open("J48_walking_cycling_NEW.model"));
            cls = (J48) ois.readObject();
            ois.close();

//            Evaluation eTest = new Evaluation(dataset);
//            eTest.evaluateModel(cls, dataset);

            String strSummary = instance.toString();
            Log.d(DEBUG_TAG, "Summary: " + strSummary);

            // Get the confusion matrix
        //    double[][] cmMatrix = eTest.confusionMatrix();

            instance.setDataset(dataset);

            // Get the likelihood of each classes
            // fDistribution[0] is the probability of being “positive”
            // fDistribution[1] is the probability of being “negative”
            double[] fDistribution = cls.distributionForInstance(instance);

            Log.d(DEBUG_TAG, "Class: " + cls.classifyInstance(instance));

            for (int i = 0; i < fDistribution.length; i++){
                Log.d(DEBUG_TAG, "Distribution: " + fDistribution[i]);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
