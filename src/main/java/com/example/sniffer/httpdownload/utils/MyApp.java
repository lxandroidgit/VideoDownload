package com.example.sniffer.httpdownload.utils;

import android.app.Application;

/**
 * Created by sniffer on 15-10-16.
 */
public class MyApp extends Application {
    private static MyApp instance;

    public static MyApp getContext(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }
}
