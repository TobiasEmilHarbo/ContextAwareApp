package com.example.tobias.contextawareapp;

/**
 * Created by rasmus on 18/12/2016.
 */

public class Aggregator {

    private String[] classes;
    private Interpreter interpreter;

    public Aggregator() {
        classes = new String[]{"cycling", "walk"};
    }

    public void addInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public double getReminders() throws Exception {
        return interpreter.interpret();
    }
}
