package com.example.homeservice;

import android.app.Application;
import com.example.homeservice.database.DatabaseHelper;

public class MyApplication extends Application {
    private static DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
    }

    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}