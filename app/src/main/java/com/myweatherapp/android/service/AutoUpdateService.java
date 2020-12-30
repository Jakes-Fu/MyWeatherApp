package com.myweatherapp.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.myweatherapp.android.WeatherActivity;
import com.myweatherapp.android.gson.AQI;
import com.myweatherapp.android.gson.Basic;
import com.myweatherapp.android.gson.Weather;
import com.myweatherapp.android.util.HttpUtil;
import com.myweatherapp.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

import static com.myweatherapp.android.util.Utility.handleAQIResponse;
import static com.myweatherapp.android.util.Utility.handleWeatherResponse;

public class AutoUpdateService extends Service {
//   public AutoUpdateService() {
//    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 1 * 60 * 60 * 1000;
//        int anHour = 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,i,0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent,flags,startId);
    }

    /**
    * 更新天气信息 */
    //这里更新天气信息的逻辑与WeatherActivity中的requestWeather（）方法的处理逻辑相似
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);

        if (weatherString != null){//获取天气id
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.getHeWeather6().get(0).getBasicX().getCid();
            String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + weatherId.toString() + "&key=3926125155d149799c364a25f652efd0";
            String aqiUrl = "https://free-api.heweather.com/s6/air/now?location=" + weatherId.toString() + "&key=3926125155d149799c364a25f652efd0";

            /**
             * 请求OKHttp更新Weather信息
             * */
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather=handleWeatherResponse(responseText);
                    if((weather != null) && "ok".equals(weather.getHeWeather6().get(0).getStatusX()))
                    {
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
//                    Log.d("AutoUpdateService","UpdateWeather" );
                }
            });
        /**
        * 请求OKHttp更新AQI信息(启用请求更新AQI，app会报空指针错误)
        * */
            HttpUtil.sendOkHttpRequest(aqiUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    AQI aqi = handleAQIResponse(responseText);
                    if((aqi != null) && "ok".equals(aqi.getHeWeather6().get(0).getStatus())) {
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
//                    Log.d("AutoUpdateService","UpdateAQI");
                }
            });
        }
//        Log.d("AutoUpdateService","UpdateWeather" );
//        Log.d("AutoUpdateService","UpdateAQI");

    }
    /**
    * 更新每日必应一图*/
    //这里更新的逻辑与WeatherActivity中的loadBingPic()方法的处理逻辑相似
    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
//        Log.d("AutoUpdateService","UpdateBingPic" );
    }


}