package com.myweatherapp.android.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{

        @SerializedName("tmp_max")//
        public String max;

        @SerializedName("tem_min")//
        public String min;
    }

    public class More{

        @SerializedName("txt_d")
        public String info;
    }


}
