package com.example.android.gps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.android.bluetoothlegatt.databinding.ActivityGpsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

public class GPSActivity extends AppCompatActivity {
    private static final int PERMISSION_FINE_LOCATION = 99;
    private ActivityGpsBinding binding;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationRequest locationRequest;

    private LocationCallback locationCallback;

    private boolean requestingLocationUpdates;

    private final String REQUESTING_LOCATION_UPDATES_KEY = "updateLocation";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateValuesFromBundle(savedInstanceState);

        binding = ActivityGpsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvLat.setText("1");
        binding.tvLon.setText("2");
        binding.tvAltitude.setText("3");
        binding.tvAccuracy.setText("4");
        binding.tvSpeed.setText("5");

        binding.tvAddress.setText("6");
//        binding.tvUpdates.setText("8");

        int locationFastestInterval = 3000;
        int locationMaxWaitTime = 100;
        int locationInterval = 100;
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    updateUIValues(location);
                }
            }
        };

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(locationFastestInterval)
                .setMaxUpdateDelayMillis(locationMaxWaitTime)
                .build();

        binding.swGps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
//                locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

                locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(locationFastestInterval)
                        .setMaxUpdateDelayMillis(locationMaxWaitTime)
                        .build();
//                binding.tvSensor.setText("Using GPS sensors");
            } else {
//                locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, locationInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(locationFastestInterval)
                .setMaxUpdateDelayMillis(locationMaxWaitTime)
                .build();
            }
//            showToast("Turn on gps!");
        });
        binding.swLocationsupdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
//            showToast("Location updated!");
        });

        updateGPS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                requestingLocationUpdates);
        // ...
        super.onSaveInstanceState(outState);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//        binding.tvUpdates.setText("Location is being tracked");
        binding.tvLat.setText("Location is being tracked");
        binding.tvLon.setText("Location is being tracked");
        binding.tvAltitude.setText("Location is being tracked");
        binding.tvAccuracy.setText("Location is being tracked");
        binding.tvSpeed.setText("Location is being tracked");
        binding.tvAddress.setText("Location is being tracked");
    }

    private void startLocationUpdates() {
//        binding.tvUpdates.setText("Location is not being tracked");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(GPSActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(GPSActivity.this, location -> {
                if (location != null)
                    updateUIValues(location);
                else
                    binding.tvAddress.setText("no location found");
            });

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {

        binding.tvLat.setText(String.valueOf(location.getLatitude()));
        binding.tvLon.setText(String.valueOf(location.getLongitude()));
        binding.tvAltitude.setText(String.valueOf(location.getAltitude()));
        if (location.hasAltitude())
            binding.tvAccuracy.setText(String.valueOf(location.getAccuracy()));
        else
            binding.tvAccuracy.setText("Not available");
        if (location.hasSpeed())
            binding.tvSpeed.setText(String.valueOf(location.getSpeed()));
        else
            binding.tvSpeed.setText("Not available");

        Geocoder geocoder = new Geocoder(GPSActivity.this);

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            binding.tvAddress.setText(addresses.get(0).getAddressLine(0));
        }catch (Exception e){
            binding.tvAddress.setText("Location not found");
        }

    }
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        // Update the value of requestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }

        // Update UI to match restored state
        updateGPS();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else{
                    showToast("No permission for GPS");
                }
                break;
        }
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}