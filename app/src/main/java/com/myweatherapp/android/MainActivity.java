package com.myweatherapp.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backButton = (ImageButton) findViewById(R.id.back_button);
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        initial();

//        if (prefs.getString("weather",null) != null){
//            backButton.setVisibility(View.GONE);
//            Intent intent = new Intent(this,WeatherActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }
    public void initial(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        backButton.setVisibility(View.GONE);
        Toast.makeText(this,"请选择你要查询的城市",Toast.LENGTH_LONG).show();
        if (prefs.getString("weather",null) != null) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}