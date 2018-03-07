package com.example.droodsunny.coolweather;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by DroodSunny on 2017/10/15.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
