package com.app.appearthquakes;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.appearthquakes.API.EarthquakeApiClient;
import com.app.appearthquakes.EarthquakeFeature;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<EarthquakeFeature> earthquakes;
    private List<EarthquakeFeature> filteredEarthquakes;
    private TextView startDateTextView;
    private TextView endDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startDateTextView = findViewById(R.id.startDateTextView);
        endDateTextView = findViewById(R.id.endDateTextView);

        Button buttonFromDate = findViewById(R.id.buttonFromDate);
        Button buttonToDate = findViewById(R.id.buttonToDate);
        Button buttonFilter = findViewById(R.id.filtrar); // Referencia al botón de filtrar

        buttonFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateTextView);
            }
        });

        buttonToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateTextView);
            }
        });

        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterEarthquakesByDate();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        String formattedStartDate = String.format("%04d-%02d-%02d", year, month, dayOfMonth);

        EarthquakeApiClient client = new EarthquakeApiClient();
        client.fetchEarthquakeData("2020-01-01", "2020-01-02", new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        Gson gson = new Gson();
                        JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                        JsonArray featuresArray = jsonObject.getAsJsonArray("features");
                        Type listType = new TypeToken<List<EarthquakeFeature>>(){}.getType();
                        earthquakes = gson.fromJson(featuresArray, listType);
                        if (mMap != null) {
                            addEarthquakeMarkers();
                        }
                    } catch (JsonSyntaxException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Error: " + response);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al realizar la solicitud", e);
            }
        });
    }

    private void filterEarthquakesByDate() {
        String startDateString = startDateTextView.getText().toString();
        String endDateString = endDateTextView.getText().toString();
        mMap.clear();

        EarthquakeApiClient client = new EarthquakeApiClient();
        client.fetchEarthquakeData(startDateString, endDateString, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        Gson gson = new Gson();
                        JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                        JsonArray featuresArray = jsonObject.getAsJsonArray("features");
                        Type listType = new TypeToken<List<EarthquakeFeature>>() {}.getType();
                        earthquakes = gson.fromJson(featuresArray, listType);
                        if (mMap != null) {
                            addEarthquakeMarkers();
                        }
                    } catch (JsonSyntaxException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Error: " + response);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al realizar la solicitud", e);
            }
        });
    }



    private void addEarthquakeMarkers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < Math.min(20, earthquakes.size()); i++) {
                    EarthquakeFeature earthquake = earthquakes.get(i);
                    Double latitude = earthquake.getGeometry().getCoordinates().get(1);
                    Double longitude = earthquake.getGeometry().getCoordinates().get(0);

                    LatLng location = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(location).title("Earthquake " + i));
                }
            }
        });
    }

    private void updateMapWithFilteredEarthquakes() {
        mMap.clear();
        for (int i = 0; i < Math.min(20, earthquakes.size()); i++) {
            EarthquakeFeature earthquake = earthquakes.get(i);
            Double latitude = earthquake.getGeometry().getCoordinates().get(1);
            Double longitude = earthquake.getGeometry().getCoordinates().get(0);

            LatLng location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(location).title("Earthquake " + i));
        }
    }

    private void showDatePickerDialog(final TextView dateTextView) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        dateTextView.setText(selectedDate);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int markerIndex = Integer.parseInt(marker.getTitle().substring(marker.getTitle().lastIndexOf(" ") + 1));

                EarthquakeFeature earthquake;
                if (earthquakes != null && earthquakes.size() > markerIndex) {
                    earthquake = earthquakes.get(markerIndex);
                } else {
                    earthquake = earthquakes.get(markerIndex);
                }

                String title = earthquake.getProperties().getTitle();
                double magnitude = earthquake.getProperties().getMag();
                double depth = earthquake.getGeometry().getCoordinates().get(2);
                String place = earthquake.getProperties().getPlace();

                StringBuilder dialogMessage = new StringBuilder();
                dialogMessage.append("Título: ").append(title).append("\n");
                dialogMessage.append("Magnitud: ").append(magnitude).append("\n");
                dialogMessage.append("Profundidad: ").append(depth).append("\n");
                dialogMessage.append("Lugar: ").append(place);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Información del Terremoto")
                        .setMessage(dialogMessage.toString())
                        .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            }
        });
    }

}
