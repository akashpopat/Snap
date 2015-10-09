package com.akashpopat.snap;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by akash on 10/9/2015.
 */
public class SnapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "Vf13WP5vyhoDkkASeXUDzvZXerVTB4iT750RE1bP", "28rMOc8SqBuyUSprME8WeOO0lMQnFl6Lxhvzl5DO");

    }
}
