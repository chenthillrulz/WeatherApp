package net.arkin.weatherapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainPage extends Activity implements View.OnClickListener, LocationListener {

    private class ViewHolder {
        Button btDetectGps;
        EditText  etCity;
    };

    private ViewHolder vh;
    private LocationManager mLocationManager;
    private static String TAG="MainPage";
    private static final long MIN_DISTANCE = 10; // meters
    private static final long MIN_TIME = 1000 * 60 * 1; // 1 minute

    private void
    HandleDetectLocation ()
    {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        setProgressBarIndeterminateVisibility(true);
        vh.btDetectGps.setEnabled(false);
        vh.etCity.setEnabled(false);
        Log.d(TAG, "Button clicked!!");
    }

    public void
    onClick(View v) {
        switch (v.getId()) {
            case R.id.button_detect_location:
                HandleDetectLocation();
                break;
            case R.id.edit_text_city:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowHomeEnabled(false);
        setContentView(R.layout.activity_main_page);

        vh = new ViewHolder();
        vh.btDetectGps = (Button) findViewById(R.id.button_detect_location);
        vh.etCity = (EditText) findViewById(R.id.edit_text_city);

        vh.etCity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (vh.etCity.getRight() - vh.etCity.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        String cityName = vh.etCity.getText().toString();

                        if (cityName.isEmpty()) {
                            Toast toast = Toast.makeText(MainPage.this,
                                    R.string.error_valid_city_name, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                            return false;
                        }

                        Intent weatherIntent = new Intent(MainPage.this, WeatherDetails.class);
                        weatherIntent.putExtra(Constants.CITY_NAME, cityName);
                        startActivity(weatherIntent);
                        return true;
                    }
                }
                return false;
            }
        });

        vh.btDetectGps.setOnClickListener(this);
        vh.etCity.setOnClickListener(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy () {
        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainPage.this);
        alertDialog.setTitle(getResources().getString(R.string.gps_settings_prompt_title));
        alertDialog.setMessage(getResources().getString(R.string.gps_enable_prompt_message));
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MainPage.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton(getResources().getString(R.string.label_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void
    resetDetector () {
        setProgressBarIndeterminateVisibility(false);
        vh.btDetectGps.setEnabled(true);
        vh.etCity.setEnabled(true);
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());

        // now go on with GeoDecoder
        //TODO is it better to call it in a thread ?
        String cityName=null;
        Geocoder gcd = new Geocoder(getBaseContext(),
                Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
                Log.d (TAG, cityName);
                Intent weatherIntent = new Intent(this, WeatherDetails.class);
                weatherIntent.putExtra(Constants.CITY_NAME, cityName);
                startActivity(weatherIntent);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this, R.string.error_geocoder, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

        resetDetector();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "GPS disabled");
        showSettingsAlert();

        setProgressBarIndeterminateVisibility(false);
        resetDetector();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "GPS enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "status " + Integer.toString(status));
        resetDetector();

        String error="";

        if (status == LocationProvider.OUT_OF_SERVICE)
            error = getResources().getString(R.string.error_gps_data_out_of_service);
        else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
            error = getResources().getString(R.string.error_gps_data_temporary);

        if (!error.isEmpty()) {
            Toast toast = Toast.makeText(this, error, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }
}
