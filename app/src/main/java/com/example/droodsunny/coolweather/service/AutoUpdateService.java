package com.example.droodsunny.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.droodsunny.coolweather.gson.Weather;
import com.example.droodsunny.coolweather.util.HttpUtil;
import com.example.droodsunny.coolweather.util.Utilty;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateWeather();
        updateBingpic();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour=4*60*60*1000;
        long tri= SystemClock.elapsedRealtime()+anHour;
        Intent intent1=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,intent1,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME,tri,pi);

        return super.onStartCommand(intent, flags, startId);
    }

    public void updateWeather(){

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);

        String responseText=preferences.getString("weather",null);
        if(responseText!=null) {
            Weather weather = Utilty.handleWeatherResponse(responseText);
            assert weather != null;
            String weatherId=weather.basic.cityName;

            String weatherUrl="https://free-api.heweather.com/v5/weather?city="+weatherId+"&key=2e15999700084b8aafa4112796f82d0c";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                           e.printStackTrace();
                        }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText=response.body().string();
                    final Weather weather=Utilty.handleWeatherResponse(responseText);

                    if(weather!=null&&"ok".equals(weather.status)){
                        //放入缓存数据
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();

                    }
                }
            });
        }


    }

    public void updateBingpic(){

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}

