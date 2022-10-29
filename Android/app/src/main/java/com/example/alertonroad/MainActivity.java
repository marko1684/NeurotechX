package com.example.alertonroad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://192.168.1.10:3000/pullUser/?id=2";


    LocationManager locationManager;
    private static MediaPlayer mp;
    Vibrator v;

    TextView textViewLatitude;
    TextView textViewLongitude;

    Button btnHit;
    Button btnAwake;
    TextView txtJson;
    ProgressDialog pd;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp = MediaPlayer.create(this, R.raw.ringtone1);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        textViewLatitude = findViewById(R.id.textview_latitude);
        textViewLongitude = findViewById(R.id.textview_longitude);
        btnHit = (Button) findViewById(R.id.btnHit);
        btnAwake = findViewById(R.id.btnAwake);
        txtJson = (TextView) findViewById(R.id.tvJsonItem);

        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JsonTask().execute(BASE_URL);
            }
        });

        btnAwake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO UPDATE server da je sleepy = 0
            }
        });


        final Handler handler = new Handler();
        final int delay = 10000; // 1000 milliseconds == 1 second

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        handler.postDelayed(new Runnable() {
            public void run() {
                System.out.println("Running location fetching every 10 seconds!"); // Do your work here
                handler.postDelayed(this, delay);

                updateLocation();
                new JsonTask().execute(BASE_URL);
            }

            private void updateLocation() {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        textViewLatitude.setText(String.valueOf(location.getLatitude()));
                        textViewLongitude.setText(String.valueOf(location.getLongitude()));
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                });
            }
        }, delay);

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson.setText(result);

            //notify
            if("1".charAt(0) == result.trim().charAt(result.length()-3)){
                Toast.makeText(MainActivity.this, "DANGEROUS DRIVER NEARBY!", Toast.LENGTH_SHORT).show();
                // Moze notifikacija
                // TODO Sound upozorenja, UPDATE server
            }

            //sleepy
            if("1".charAt(0) == result.trim().charAt(result.length()-14)){
                Toast.makeText(MainActivity.this, "Pull Over And Take A Power Nap!", Toast.LENGTH_SHORT).show();
                if (mp.isPlaying()) {
                    mp.seekTo(0);
                } else {
                    mp.start();
                    v.vibrate(3000);
                }
            }
        }
    }

}