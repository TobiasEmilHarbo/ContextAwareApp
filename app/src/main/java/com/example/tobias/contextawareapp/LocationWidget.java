package com.example.tobias.contextawareapp;

import android.content.Context;

/**
 * Created by Tobias on 17/12/2016.
 */

public class LocationWidget {

    private Context context;

    public LocationWidget(Context context){
        this.context = context;
    }

    private Context getContext(){
        return context;
    }
}
