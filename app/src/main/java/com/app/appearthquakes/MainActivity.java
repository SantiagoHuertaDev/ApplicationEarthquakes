package com.app.appearthquakes;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.app.appearthquakes.API.EarthquakeApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, EarthquakeAdapter.OnItemClickListener {

    private GoogleMap mMap;
    private List<EarthquakeFeature> earthquakes;
    private Button buttonFromDate;
    private Button buttonToDate;
    private Button buttonClose;
    private TextView textViewNotEarthquakes;
    private RecyclerView recyclerView;
    private ImageView imageViewProfile;
    private TextView textViewTitle;
    private TextView textViewMagnitude;
    private TextView textViewDepth;
    private TextView textViewPlace;
    private User user;
    private List<User> userList;
    private ConstraintLayout earthquakeInfoLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        AppDatabase appDatabase = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "dbPruebas"
        ).allowMainThreadQueries().build();

        userList = appDatabase.daoUser().getUsers();
        SharedPreferences prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        String emailUser = prefs.getString("userEmail", "");
        for (User u : userList) {
            if (u.getEmail().equals(emailUser)) {
                user = u;
                break;
            }
        }

        loadComponent();
        getEarthquakesByAPI();
        Toast.makeText(this, "Actualizando mapa", Toast.LENGTH_LONG).show();
    }

    private void getEarthquakesByAPI() {
        EarthquakeApiClient client = new EarthquakeApiClient();
        client.fetchEarthquakeData("2020-01-01", "2020-01-02", new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        Gson gson = new Gson();
                        JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                        JsonArray featuresArray = jsonObject.getAsJsonArray("features");
                        Type listType = new TypeToken<List<EarthquakeFeature>>() {
                        }.getType();
                        earthquakes = gson.fromJson(featuresArray, listType);
                        if (mMap != null) {
                            addEarthquakeMarkers();
                            loadEarthquakeRecycler();

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

    private void loadComponent() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buttonFromDate = findViewById(R.id.buttonFromDate);
        buttonToDate = findViewById(R.id.buttonToDate);
        textViewNotEarthquakes = findViewById(R.id.textViewNotEarthquakes);
        imageViewProfile = findViewById(R.id.ImageViewProfile);
        recyclerView = findViewById(R.id.recyclerViewEarthquakes);
        earthquakeInfoLayout = findViewById(R.id.earthquake_info_layout);
        buttonClose = findViewById(R.id.buttonClose);
        textViewTitle = findViewById(R.id.titleCL);
        textViewMagnitude = findViewById(R.id.magnitudeCL);
        textViewDepth = findViewById(R.id.depthCL);
        textViewPlace = findViewById(R.id.placeCL);
        buttonToDate.setEnabled(false);

        buttonFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(buttonFromDate);
            }
        });

        buttonToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(buttonToDate);
            }
        });


        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        Button buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConstraintLayout earthquakeInfoLayout = findViewById(R.id.earthquake_info_layout);
                earthquakeInfoLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void filterEarthquakesByDate() {
        buttonToDate.setEnabled(false);
        earthquakeInfoLayout.setVisibility(View.INVISIBLE);
        String startDateString = buttonFromDate.getText().toString();
        String endDateString = buttonToDate.getText().toString();
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
                        Type listType = new TypeToken<List<EarthquakeFeature>>() {
                        }.getType();
                        earthquakes = gson.fromJson(featuresArray, listType);
                        if (mMap != null) {
                            addEarthquakeMarkers();
                            loadEarthquakeRecycler();
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
        Toast.makeText(this, "Actualizando mapa", Toast.LENGTH_LONG).show();
    }

    private void zoomOutMap() {
        LatLng defaultLocation = new LatLng(0, 0);
        float defaultZoom = 2;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom));
    }

    private void addEarthquakeMarkers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int startIndex = Math.max(0, earthquakes.size() - 20);
                for (int i = startIndex; i < earthquakes.size(); i++) {
                    EarthquakeFeature earthquake = earthquakes.get(i);
                    Double latitude = earthquake.getGeometry().getCoordinates().get(1);
                    Double longitude = earthquake.getGeometry().getCoordinates().get(0);

                    LatLng location = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(location).title("Earthquake " + i));
                }
            }
        });
    }

    public void loadEarthquakeRecycler() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (earthquakes != null && !earthquakes.isEmpty()) {
                    List<EarthquakeFeature> last20Earthquakes;
                    if (earthquakes.size() <= 20) {
                        last20Earthquakes = new ArrayList<>(earthquakes);
                    } else {
                        last20Earthquakes = earthquakes.subList(earthquakes.size() - 20, earthquakes.size());
                    }
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    EarthquakeAdapter adapter = new EarthquakeAdapter(last20Earthquakes, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                }else{
                    recyclerView.setAdapter(null);
                    earthquakeInfoLayout.setVisibility(View.VISIBLE);
                    textViewNotEarthquakes.setVisibility(View.VISIBLE);
                    buttonClose.setVisibility(View.INVISIBLE);
                    textViewTitle.setVisibility(View.INVISIBLE);
                    textViewMagnitude.setVisibility(View.INVISIBLE);
                    textViewDepth.setVisibility(View.INVISIBLE);
                    textViewPlace.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void showDatePickerDialog(final Button dateButton) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;

                        if (dateButton.getId() == R.id.buttonFromDate) {
                            dateButton.setText(selectedDate);
                            buttonToDate.setEnabled(true);
                        } else if (dateButton.getId() == R.id.buttonToDate) {
                            String fromDateText = buttonFromDate.getText().toString();
                            if (isDateAfter(selectedDate, fromDateText) || selectedDate.equals(fromDateText)) {
                                dateButton.setText(selectedDate);
                                filterEarthquakesByDate();
                                zoomOutMap();
                            } else {
                                Toast.makeText(MainActivity.this, "Seleccione una fecha posterior o igual a la de inicio", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private boolean isDateAfter(String dateToCheck, String dateReference) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dateCheck = sdf.parse(dateToCheck);
            Date dateRef = sdf.parse(dateReference);
            return dateCheck.after(dateRef);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("ResourceType")
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.xml.menu_profile, popupMenu.getMenu());

        MenuItem name = popupMenu.getMenu().findItem(R.id.action_profile_name);
        MenuItem logOut = popupMenu.getMenu().findItem(R.id.action_logout);

        if (user != null) {
            name.setTitle(user.getName() + " " + user.getLastName());
        }

        logOut.setOnMenuItemClickListener(item -> {
            SharedPreferences prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("userLogged", false);
            editor.apply();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        });

        popupMenu.show();
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

    @Override
    public void onItemClick(EarthquakeFeature earthquake) {
        textViewNotEarthquakes.setVisibility(View.INVISIBLE);
        earthquakeInfoLayout.setVisibility(View.VISIBLE);
        buttonClose.setVisibility(View.VISIBLE);
        textViewTitle.setVisibility(View.VISIBLE);
        textViewMagnitude.setVisibility(View.VISIBLE);
        textViewDepth.setVisibility(View.VISIBLE);
        textViewPlace.setVisibility(View.VISIBLE);

        String title = earthquake.getProperties().getTitle();
        double magnitude = earthquake.getProperties().getMag();
        double depth = earthquake.getGeometry().getCoordinates().get(2);
        String place = earthquake.getProperties().getPlace();

        textViewTitle.setText("Título: " + title);
        textViewMagnitude.setText("Magnitud: " + magnitude);
        textViewDepth.setText("Profundidad: " + depth);
        textViewPlace.setText("Lugar: " + place);

        Double latitude = earthquake.getGeometry().getCoordinates().get(1);
        Double longitude = earthquake.getGeometry().getCoordinates().get(0);
        LatLng location = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
    }

}
