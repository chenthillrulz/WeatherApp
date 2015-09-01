package net.arkin.weatherapp;

import java.util.List;

/**
 * Created by Chenthill on 9/2/2015.
 */
public class WeatherData {
    public static String ICON_BASE_URL="http://openweathermap.org/img/w/";
    public static String EXT=".png";

    public Main main;
    public List<Weather> weather;
    public long visibility;
    public Wind wind;
    public String name;

    public class Main {
        public float temp;
        public int humidity;
        public float temp_min;
        public float temp_max;
    }

    public class Weather {
        public String icon;
    }

    public class Visibility {
        public long value;
    }

    public class Wind {
        public float speed;
    }
}
