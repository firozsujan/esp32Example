/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import com.example.android.bluetoothlegatt.databinding.ActivityDeviceScanBinding;
import com.example.android.gps.GPSActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    List<BluetoothDevice> bluetoothDevices;


    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner bluetoothLeScanner;
    private ActivityDeviceScanBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDeviceScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestPermission();

        getSupportActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //start scanning bluetooth
        binding.btnScan.setOnClickListener(v -> {
            if(!mScanning) {
                mLeDeviceListAdapter.clear();
                bluetoothDevices.clear();
                scanLeDevice(true);
                binding.btnScan.setText("Stop");
            } else{
                scanLeDevice(false);
                binding.btnScan.setText("Scan");
            }
        });

        // connect with selected devices
        binding.btnConnect.setOnClickListener(v -> {
            if(bluetoothDevices.size()>0){
                final Intent intent = new Intent(DeviceScanActivity.this, DeviceControlActivity.class);
                int index = 0;
                for (BluetoothDevice device : bluetoothDevices) {
                    String EXTRAS_DEVICE_NAME = DeviceControlActivity.EXTRAS_DEVICE_NAME+index;
                    String EXTRAS_DEVICE_ADDRESS = DeviceControlActivity.EXTRAS_DEVICE_ADDRESS+index;
                    intent.putExtra(EXTRAS_DEVICE_NAME, device.getName());
                    intent.putExtra(EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    index++;
                }
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_INDEX, index);
                startActivity(intent);
            } else{
                Toast.makeText(getApplicationContext(), "Please select a device from the List", Toast.LENGTH_LONG).show();
            }
        });

        // go to gps activity
        binding.btnGps.setOnClickListener(v -> {
            Intent intent = new Intent(DeviceScanActivity.this, GPSActivity.class);
            startActivity(intent);
        });

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();

        bluetoothDevices = new ArrayList<>();
        binding.lvDevice.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//        binding.lvDevice.setItemsCanFocus(false);
        binding.lvDevice.setAdapter(mLeDeviceListAdapter);
        binding.lvDevice.setOnItemClickListener((parent, view, position, id) -> {
            CheckBox checkbox = view.findViewById(R.id.cb_is_selected);
            if(!checkbox.isChecked()) {

                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                bluetoothDevices.add(device);
                if (mScanning) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bluetoothLeScanner.stopScan(mScanCallback);
                    }
                    mScanning = false;
                }
                checkbox.setChecked(true);
            } else{
                checkbox.setChecked(false);
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                bluetoothDevices.remove(device);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        menu.findItem(R.id.menu_gps).setVisible(true);
//        if (!mScanning) {
//            menu.findItem(R.id.menu_stop).setVisible(false);
//            menu.findItem(R.id.menu_scan).setVisible(true);
//            MenuItemCompat.setActionView(menu.findItem(R.id.menu_refresh), null);
//        } else {
//            menu.findItem(R.id.menu_stop).setVisible(true);
//            menu.findItem(R.id.menu_scan).setVisible(false);
//            menu.findItem(R.id.menu_refresh).setActionView(
//                    R.layout.actionbar_indeterminate_progress);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.menu_scan:
//                mLeDeviceListAdapter.clear();
//                bluetoothDevices.clear();
//
//                scanLeDevice(true);
//                break;
//            case R.id.menu_stop:
//                scanLeDevice(false);
//                break;
//            case R.id.menu_gps:
////                Intent intent = new Intent(DeviceScanActivity.this, GPSActivity.class);
////                startActivity(intent);
//                final Intent intent = new Intent(this, DeviceControlActivity.class);
//                int index = 0;
//                for (BluetoothDevice device : bluetoothDevices) {
//                    String EXTRAS_DEVICE_NAME = DeviceControlActivity.EXTRAS_DEVICE_NAME+index;
//                    String EXTRAS_DEVICE_ADDRESS = DeviceControlActivity.EXTRAS_DEVICE_ADDRESS+index;
//                    intent.putExtra(EXTRAS_DEVICE_NAME, device.getName());
//                    intent.putExtra(EXTRAS_DEVICE_ADDRESS, device.getAddress());
//                    index++;
//
//                }
//                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_INDEX, index);
//                startActivity(intent);
//                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

//        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }
//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//        CheckBox checkbox = (CheckBox) findViewById(R.id.checkbox);
//        if(checkbox.isChecked()) {
//            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
//            if (device == null) return;
//            bluetoothDevices.add(device);
//            if (mScanning) {
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    bluetoothLeScanner.stopScan(mScanCallback);
//                }
//                mScanning = false;
//            }
//        } else{
//            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
//            if (device == null) return;
//            bluetoothDevices.remove(device);
//        }
//
//    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mLeDeviceListAdapter.clear();
            bluetoothDevices.clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                mScanning = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bluetoothLeScanner.stopScan(mScanCallback);
                }
                invalidateOptionsMenu();
            }, SCAN_PERIOD);

            mScanning = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner.startScan(mScanCallback);
            }
        } else {
            mScanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner.stopScan(mScanCallback);
            }
        }
        invalidateOptionsMenu();
    }
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check.
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        private BluetoothDevice device;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.isSelected = (CheckBox) view.findViewById(R.id.cb_is_selected);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            device = mLeDevices.get(i);

            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            if (viewHolder.isSelected.isChecked()){
                viewHolder.isSelected.setChecked(false);
            }

            return view;
        }
    }

    // Device scan callback.
//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//
//        @Override
//        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mLeDeviceListAdapter.addDevice(device);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//    };
    // Device scan callback.
    private ScanCallback mScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    binding.btnScan.setText("Scan");
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
                @Override
                public void onScanFailed(int error){
                    super.onScanFailed(error);
                    binding.btnScan.setText("Scan");
                    Log.d("ScanFailed", "errorCode: "+error);

                }
            };
//    private ScanCallback mLeScanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//        }
//
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//        }
//    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        CheckBox isSelected;
    }

}
