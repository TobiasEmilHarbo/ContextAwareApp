package com.example.tobias.contextawareapp;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.InputStream;


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

        new CalendarWidget();

        try {

            InputStream model = getAssets().open("J48_walking_cycling_NEW.model");

            Interpreter interpreter = new Interpreter(model);
            double classification = interpreter.interpret(
                    new String[]{"cycling", "walk"},
                    new String[]{"min", "max"},
                    new Double[]{-7.440355312, 5.97923057}
            );

            Log.d(DEBUG_TAG, "Class: " + classification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
