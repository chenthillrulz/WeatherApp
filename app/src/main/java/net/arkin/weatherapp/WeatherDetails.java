package net.arkin.weatherapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

//TODO add a scroll view
public class WeatherDetails extends Activity {

    private class ViewHolder {
        TextView tvTemperature;
        ImageView ivClouds;
        TextView tvMinTemp;
        TextView tvMaxTemp;
        TextView tvHumidity;
        TextView tvWindSpeed;
        TextView tvVisibility;
    };

    private ViewHolder vh;
    private EventBus mBus;
    private static String TAG="WeatherDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowHomeEnabled(false);
        setContentView(R.layout.activity_weather_details);

        vh = new ViewHolder();
        vh.tvTemperature = (TextView) findViewById(R.id.textViewTemperature);
        vh.ivClouds = (ImageView) findViewById(R.id.imageViewCloud);
        vh.tvMinTemp = (TextView) findViewById(R.id.textViewMinTemp);
        vh.tvMaxTemp = (TextView) findViewById(R.id.textViewMaxTemp);
        vh.tvHumidity = (TextView) findViewById(R.id.textViewHumidity);
        vh.tvWindSpeed = (TextView) findViewById(R.id.textViewWindSpeed);
        vh.tvVisibility = (TextView) findViewById(R.id.textViewVisibility);

        mBus = new EventBus();

        String cityName = getIntent().getStringExtra(Constants.CITY_NAME);
        getActionBar().setTitle(cityName);
        setProgressBarIndeterminateVisibility(true);

        // Used the EventBus/Retrofit combination to avoid activity leaks
        new RestClient().getApiService().getWeatherData(cityName, new Callback<WeatherData>() {
            EventBus lBus = mBus;

            @Override
            public void success(WeatherData weatherData, Response response) {
                lBus.postSticky(weatherData);
            }

            @Override
            public void failure(RetrofitError error) {
                lBus.postSticky(error);
                Log.d(TAG, "Error fetching data " + error.toString());
            }
        });
    }

    public void onEventMainThread (WeatherData weatherData)
    {
        Log.d(TAG, weatherData.name);
        setProgressBarIndeterminateVisibility(false);
        Double temp = weatherData.main.temp - 272.15; // kelvin to celcius
        vh.tvTemperature.setText(Integer.toString(temp.intValue()));

        String url = WeatherData.ICON_BASE_URL + weatherData.weather.get(0).icon + WeatherData.EXT;
        Log.d(TAG, url);
        Picasso.with(this).load(url).into(vh.ivClouds);

        temp = weatherData.main.temp_min - 272.15;
        String minTemp = getResources().getString(R.string.label_max) + " "
                + Integer.toString(temp.intValue()) + getString(R.string.label_degrees);
        vh.tvMinTemp.setText(minTemp);

        temp = weatherData.main.temp_max - 272.15;
        String maxTemp = getResources().getString(R.string.label_max) + " "
                + Integer.toString(temp.intValue()) + getString(R.string.label_degrees);
        vh.tvMaxTemp.setText(maxTemp);

        String humidity = Integer.toString(weatherData.main.humidity) + "%";
        vh.tvHumidity.setText(humidity);

        Float windSpeed = (weatherData.wind.speed / 1000) * 3600; // meter/sec to km/hr
        String windSpeedStr = Integer.toString(windSpeed.intValue()) + " " + "Kmph";
        vh.tvWindSpeed.setText(windSpeedStr);

        String visibility =Long.toString(weatherData.visibility/1000) + "K"; // meter to Km
        vh.tvVisibility.setText(visibility);

        mBus.removeStickyEvent(weatherData);
    }

    public void onEventMainThread (RetrofitError error)
    {
        setProgressBarIndeterminateVisibility(false);

        Toast toast = Toast.makeText(this, R.string.error_fetch_openweathermap, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();

        mBus.removeStickyEvent(error);
    }

    @Override
    public void
    onPause () {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    public void
    onResume () {
        super.onResume();
        mBus.registerSticky(this);
    }

    @Override
    public void
    onDestroy () {
        super.onDestroy();
        mBus.unregister(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}
