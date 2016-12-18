package com.example.tobias.contextawareapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            InputStream model = getAssets().open("J48_walking_cycling_NEW.model");
            Aggregator aggregator = new Aggregator();
            Interpreter activityAndLocationInterpreter = new Interpreter(model);
            //activityAndLocationInterpreter.addLocationWidget();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
