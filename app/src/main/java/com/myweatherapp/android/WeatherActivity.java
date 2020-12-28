package com.myweatherapp.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.myweatherapp.android.gson.AQI;
import com.myweatherapp.android.gson.Weather;
import com.myweatherapp.android.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.myweatherapp.android.util.Utility.handleWeatherResponse;
import static com.myweatherapp.android.util.Utility.handleAQIResponse;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
//    private ImageButton navButton;
    private TextView navText;

    @Override
    protected void onCreate(Bundle savedInstanceState)   {
        super.onCreate(savedInstanceState);
        /**
         *判断android系统是否在5.0(版本号大于等于21)及以上；若满足，则调用app与系统状态栏融为一体；反之...
         * */
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

       /**
        * 初始化各个控件*/
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navText = (TextView) findViewById(R.id.nav_text);

        swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary_dark);//刷新控件的颜色

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);

      //  final String weatherId;
        if (weatherString != null){//获取天气id
            Weather weather = (Weather) handleWeatherResponse(weatherString);
            mWeatherId = weather.getHeWeather6().get(0).getBasicX().getCid();
            showWeatherInfo(weather);
        }else {
//            String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {    //为swipeRefresh设置一个监听器，用于刷新天气信息
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPic = prefs.getString("bing_pc",null);       //加载每日必应一图，如果初始化没有图片则执行loadBIngPic；若有，则刷新
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
        navText.setOnClickListener(new View.OnClickListener() {           //为home键设置监听器，点击home键可以切换城市
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

/**lvno6VKDYvuNS8fk   9ee8b89320a54f588269c9c13f4278e5  http://guolin.tech/api/china
* 根据天气id请求城市天气信息*/
    public void requestWeather(final String weatherId) {

        final String weatherUrl = "https://free-api.heweather.com/s6/weather?location="+weatherId.toString()+"&key=5cfa71f0523045cbbc2a915848c89ad4";
        final String aqiUrl="https://free-api.heweather.com/s6/air/now?location="+weatherId.toString()+"&key=5cfa71f0523045cbbc2a915848c89ad4";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气预报信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ( weather != null && "ok".equals(weather.getHeWeather6().get(0).getStatusX())){
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.getHeWeather6().get(0).getBasicX().getCid();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        HttpUtil.sendOkHttpRequest(aqiUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final AQI aqi = handleAQIResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAQIInfo(aqi);
                        if((aqi != null) && "ok".equals(aqi.getHeWeather6().get(0).getStatus()))
                        {
                            SharedPreferences.Editor editor =
                                    PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId=aqi.getHeWeather6().get(0).getBasic().getCid();
                            showAQIInfo(aqi);
                        }else
                        {
                            Toast.makeText(WeatherActivity.this, responseText, Toast.LENGTH_SHORT).show();

                        }
                        swipeRefresh.setRefreshing(false);
                    }

                });
            }
        });
        loadBingPic();
    }
    /**
     * 加载天气App背景图片，该背景图片每日都会自动更换
     * */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
/**
 * 展示weather实体类中的AQI指数信息*/
    private void showAQIInfo(AQI aqi) {
        if(aqi != null) {
            aqiText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getAqi());
            pm25Text.setText(aqi.getHeWeather6().get(0).getAir_now_city().getPm25());
        }
    }

    /**
 * 处理并展示weather实体类中的各种数据*/
    private void showWeatherInfo(Weather weather) {

//        String cityName = weather.basic.cityName;
//        String updateTime = weather.basic.update.updateTime.split(" ")[1];
//        String degree = weather.now.temperature + "℃";
//        String weatherInfo = weather.now.more.info;
        String cityName = weather.getHeWeather6().get(0).getBasicX().getLocation();
//        String updateTime = weather.getHeWeather6().get(0).getUpdate().getLoc();
        String degree = weather.getHeWeather6().get(0).getNowX().getTmp()+"℃";
        String weatherInfo = weather.getHeWeather6().get(0).getNowX().getCond_txt();
        titleCity.setText(cityName);
//        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();


        for(int i=0;i<3;i++ ) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            if (i == 0) {
                dateText.setText("  今天");
            }else {
                dateText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getDate());
            }
            infoText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getCond_txt_n());
            maxText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_max());
            minText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_min());

//            dateText.setText(forecast.date);
//            infoText.setText(forecast.more.info);
//            maxText.setText(forecast.temperature.max);
//            minText.setText(forecast.temperature.min);

            forecastLayout.addView(view);
        }
        comfortText.setText("舒适度："+weather.getHeWeather6().get(0).getLifestyle().get(0).getTxt());
        carWashText.setText("洗车指数："+weather.getHeWeather6().get(0).getLifestyle().get(6).getTxt());
        sportText.setText("运动指数："+weather.getHeWeather6().get(0).getLifestyle().get(3).getTxt());

        weatherLayout.setVisibility(View.VISIBLE);
//        Intent intent=new Intent(this, AutoUpdateService.class);
//        startService(intent);



//        if (weather.aqi != null){
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
//        String comfort = "舒适度：" + weather.suggestion.comfort.info;
//        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
//        String sport = "运动建议：" + weather.suggestion.sport.info;
//        comfortText.setText(comfort);
//        carWashText.setText(carWash);
//        sportText.setText(sport);
//        weatherLayout.setVisibility(View.VISIBLE);
    }

}