package com.example.tobias.contextawareapp;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
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

            InputStream model = getAssets().open("J48_walking_cycling_NEW.model");

            double classification = Interpreter.interpret(model,
                    new String[]{"cycling", "walk"},
                    new String[]{"min", "max"},
                    new Double[]{3.551, 24.5121}
            );
            Log.d(DEBUG_TAG, "Class: " + classification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
