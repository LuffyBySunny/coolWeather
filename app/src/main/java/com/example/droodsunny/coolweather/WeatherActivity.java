package com.example.droodsunny.coolweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.example.droodsunny.coolweather.gson.Forecast;
import com.example.droodsunny.coolweather.gson.Weather;
import com.example.droodsunny.coolweather.service.AutoUpdateService;
import com.example.droodsunny.coolweather.util.HttpUtil;
import com.example.droodsunny.coolweather.util.Utilty;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public TextView title_city;
    private TextView title_update_time;
    private String province;
    private String city;
    private String county;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private CoordinatorLayout weatherLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    public LocationClient mLocationClient = null;
    private SharedPreferences prefs;
    private ImageView image;
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    private int offest;
    private Toolbar toolbar;

    private AppBarLayout appBarLayout;

    private NestedScrollView nestedScrollView;

    public CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Intent intent= new Intent(WeatherActivity.this, AutoUpdateService.class);
        startService(intent);

        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener( new MyLocationListener());

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        weatherLayout=(CoordinatorLayout) findViewById(R.id.weatherLayout);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        title_city=(TextView)findViewById(R.id.title_city);
        title_update_time=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        image=(ImageView)findViewById(R.id.bing_pic_img);
        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        nestedScrollView= (NestedScrollView) findViewById(R.id.nested);
        appBarLayout= (AppBarLayout) findViewById(R.id.appBar);
        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);

        appBarLayout.setBackgroundColor(0);

        weatherLayout.setVisibility(View.INVISIBLE);

        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
       if(actionBar!=null) {
           actionBar.setDisplayHomeAsUpEnabled(true);
           actionBar.setHomeAsUpIndicator(R.mipmap.ic_home);

       }
       collapsingToolbarLayout.setExpandedTitleTextColor(ColorStateList.valueOf(Color.WHITE));
       collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

        collapsingToolbarLayout.getTitle();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(title_city.getText().toString());
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                offest=verticalOffset;
            }
        });

        prefs= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(image);
        }else {
            loadBingPic();
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(WeatherActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else {
            requestLocate();
        }

       /* homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });*/
        /*手动切换城市*/
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存，从缓存读取
            Weather weather= Utilty.handleWeatherResponse(weatherString);
            county=weather.basic.cityName;
            showWeatherInfo(weather);
        }else {
            county=getIntent().getStringExtra("countyName");
            requestWeather(county);
        }

        nestedScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(offest!=0&&!swipeRefresh.isRefreshing()){
                    swipeRefresh.setEnabled(false);
                }else {
                    swipeRefresh.setEnabled(true);
                }
                return false;
            }
        });



    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(image);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void requestWeather(final String weatherId){
        String weatherUrl="https://free-api.heweather.com/v5/weather?city="+weatherId+"&key=2e15999700084b8aafa4112796f82d0c";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utilty.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            //放入缓存数据
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }
    private void showWeatherInfo(Weather weather){

        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        title_update_time.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
       /* Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);*/
    }
    private void requestLocate(){
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
                   province=bdLocation.getProvince().substring(0,bdLocation.getProvince().length()-1);
                   city=bdLocation.getCity().substring(0,bdLocation.getCity().length()-1);
                   county=bdLocation.getDistrict().substring(0,bdLocation.getDistrict().length()-1);
                   title_city.setText(county);
                   collapsingToolbarLayout.setTitle(county);
                   String weatherString=prefs.getString("weather",null);
                  if(weatherString!=null){
                      //有缓存，从缓存读取
                      Weather weather= Utilty.handleWeatherResponse(weatherString);
                      String countyName=weather.basic.cityName;
                      if(countyName==county){
                         showWeatherInfo(weather);
                      }else {
                          requestWeather(county);
                      }

                  }else {
                     requestWeather(county);
                 }
        }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"没有同意权限，程序即将退出",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocate();
                }else {
                    Toast.makeText(this,"发生未知错误，程序即将退出",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }
}
