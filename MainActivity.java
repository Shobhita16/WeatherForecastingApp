package com.example.weatheringforecasterapp;

import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // UI components
    EditText cityName;
    Button search;
    TextView show;
    ImageView weatherBackground; // Background ImageView
    ImageView weatherIcon; // Weather icon ImageView

    String url;

    // Inner class to handle weather fetching asynchronously
    class GetWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject main = jsonObject.getJSONObject("main");
                    JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
                    String condition = weather.getString("main"); // Get the weather condition

                    // Extract temperatures from the JSON object
                    double tempKelvin = main.getDouble("temp");
                    double feelsLikeKelvin = main.getDouble("feels_like");
                    double tempMaxKelvin = main.getDouble("temp_max");
                    double tempMinKelvin = main.getDouble("temp_min");

                    double tempCelsius = tempKelvin - 273.15;
                    double feelsLikeCelsius = feelsLikeKelvin - 273.15;
                    double tempMaxCelsius = tempMaxKelvin - 273.15;
                    double tempMinCelsius = tempMinKelvin - 273.15;

                    String weatherInfo = "Temperature: " + String.format("%.2f", tempCelsius) + "°C\n" +
                            "Feels like: " + String.format("%.2f", feelsLikeCelsius) + "°C\n" +
                            "Max: " + String.format("%.2f", tempMaxCelsius) + "°C\n" +
                            "Min: " + String.format("%.2f", tempMinCelsius) + "°C\n" +
                            "Pressure: " + main.getString("pressure") + " hPa";

                    show.setText(weatherInfo);

                    // Set the background image and icon based on weather condition
                    switch (condition.toLowerCase()) {
                        case "clear":
                            weatherBackground.setImageResource(R.drawable.background_clear);
                            weatherIcon.setImageResource(R.drawable.icon_clear);
                            break;
                        case "rain":
                            weatherBackground.setImageResource(R.drawable.background_rain);
                            weatherIcon.setImageResource(R.drawable.icon_rain);
                            break;
                        case "clouds":
                            weatherBackground.setImageResource(R.drawable.background_cloudy);
                            weatherIcon.setImageResource(R.drawable.icon_cloudy);
                            break;
                        case "snow":
                            weatherBackground.setImageResource(R.drawable.background_snow);
                            weatherIcon.setImageResource(R.drawable.icon_snow);
                            break;
                        default:
                            weatherBackground.setImageResource(R.drawable.background_default);
                            weatherIcon.setImageResource(R.drawable.icon_default);
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                show.setText("Unable to find weather");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect UI elements
        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search_button);
        show = findViewById(R.id.weather);
        weatherBackground = findViewById(R.id.weatherBackground);
        weatherIcon = findViewById(R.id.weatherIcon);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString();
                if (!city.isEmpty()) {
                    url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=47f0d2a44b5965ebaef9c2fb5eb6bf42";

                    GetWeather task = new GetWeather();
                    try {
                        task.execute(url).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
