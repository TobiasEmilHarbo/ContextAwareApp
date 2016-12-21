package com.example.tobias.contextawareapp;

/**
 * Created by Tobias on 19/12/2016.
 */

public interface Interpreter {
    double interpret(String[] classes, String[] attributesArray, Double[] data) throws Exception;
    void addWidget(String key, Widget widget);
    double interpret() throws Exception;
}
