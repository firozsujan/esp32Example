package com.example.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.DeviceScanActivity;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.databinding.ActivityGpsBinding;
import com.example.android.bluetoothlegatt.databinding.ActivityMainBinding;
import com.example.android.gps.GPSActivity;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_FINE_LOCATION = 99;

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestPermission();
        binding.btnBluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
            startActivity(intent);
        });
        binding.btnGpsActivity.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GPSActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("Permission granted for GPS");
                } else{
                    showToast("No permission for GPS");
                }
                break;
        }
    }

    private void requestPermission() {
        LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check.
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_FINE_LOCATION);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }
    //toast message function
    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}