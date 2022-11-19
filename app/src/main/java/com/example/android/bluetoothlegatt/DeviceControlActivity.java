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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.bluetoothlegatt.databinding.ActivityDeviceControlBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static String DEVICE_ONE = "";
    public static String DEVICE_TWO = "";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_INDEX = "index";
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;

    //    private String mDeviceName;
//    private String mDeviceAddress;
//    private ExpandableListView mGattServicesList;
//    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics2 =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private boolean mConnected2 = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic2;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private final String TURN_ON = "N";
    private final String TURN_OFF = "F";
    private List<MyDevice> myDevices;
    private List<ServiceConnection> serviceConnections = new ArrayList<>();
    private ActivityDeviceControlBinding binding;

    // Code to manage Service lifecycle.
//    private ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
//            }
//            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(mDeviceAddress);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_device_control);
        binding = ActivityDeviceControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestPermission();
        final Intent intent = getIntent();
        Log.d("EXTRAS_DEVICE_INDEX", "" + intent.getIntExtra(EXTRAS_DEVICE_INDEX, 0));
        //    private TextView mConnectionState;
        //    private TextView mDataField;
        //    private Button mOn;
        //    private Button mOff;
        int mDeviceIndex = intent.getIntExtra(EXTRAS_DEVICE_INDEX, 0);
        myDevices = new ArrayList<>();
        for (int i = 0; i < mDeviceIndex; i++) {
            String name = intent.getStringExtra(EXTRAS_DEVICE_NAME + i);
            String address = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS + i);
            Log.d("DeviceName:", name);
            MyDevice myDevice = new MyDevice(name, address);
            myDevices.add(myDevice);
            if (DEVICE_ONE == "") {
                DEVICE_ONE = address;
                ServiceConnection mServiceConnection = initService(myDevice);
                serviceConnections.add(mServiceConnection);
                Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }
            if (DEVICE_TWO == "" && i == 1) {
                DEVICE_TWO = address;
                ServiceConnection mServiceConnection = initService(myDevice);
                serviceConnections.add(mServiceConnection);
                Intent gattServiceIntent = new Intent(this, BluetoothLeServiceTwo.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }

        }
        // Sets up UI references.
        initUiComponents();


        getSupportActionBar().setTitle("Sensors Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//
//        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    private void initUiComponents() {
        if (myDevices.size() > 0) {
            //setting up 1st device data
            MyDevice device1 = myDevices.get(0);
            binding.deviceName.setText(device1.getName());
            binding.deviceAddress.setText(device1.getAddress());
//            mGattServicesList = findViewById(R.id.gatt_services_list);
//            binding.gattServicesList.setOnChildClickListener(servicesListClickListner);

            binding.buttonOn.setOnClickListener(v -> device1.getBluetoothLeService().writeCharacteristic(TURN_ON.getBytes()));
            binding.buttonOff.setOnClickListener(v -> device1.getBluetoothLeService().writeCharacteristic(TURN_OFF.getBytes()));
            binding.buttonConnect.setOnClickListener(v -> {
                if (!mConnected) {
                    binding.buttonConnect.setText(R.string.menu_connecting);
                    device1.getBluetoothLeService().connect(device1.getAddress());
                } else {
                    binding.buttonConnect.setText(R.string.menu_connect);
                    device1.getBluetoothLeService().disconnect();
                }
            });
            if (myDevices.size() > 1) {
                MyDevice device2 = myDevices.get(1);
                binding.deviceName2.setText(device2.getName());
                binding.deviceAddress2.setText(device2.getAddress());
//            mGattServicesList = findViewById(R.id.gatt_services_list);
//                binding.gattServicesList2.setOnChildClickListener(servicesListClickListner2);

                binding.buttonOn2.setOnClickListener(v -> device2.getBluetoothLeServiceTwo().writeCharacteristic(TURN_ON.getBytes()));
                binding.buttonOff2.setOnClickListener(v -> device2.getBluetoothLeServiceTwo().writeCharacteristic(TURN_OFF.getBytes()));
                binding.buttonConnect2.setOnClickListener(v -> {
                    if (!mConnected2) {
                        binding.buttonConnect2.setText(R.string.menu_connecting);
                        device2.getBluetoothLeServiceTwo().connect(device2.getAddress());
                    } else {
                        binding.buttonConnect2.setText(R.string.menu_connect);
                        device2.getBluetoothLeServiceTwo().disconnect();
                    }
                });
            }
        } else {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        for (MyDevice device : myDevices) {
            if (device.getBluetoothLeService() != null) {
                boolean result = device.getBluetoothLeService().connect(device.getAddress());
                Log.d(TAG, "Connect request result=" + result);
            }
            if (device.getBluetoothLeServiceTwo() != null) {
                boolean result = device.getBluetoothLeServiceTwo().connect(device.getAddress());
                Log.d(TAG, "Connect request result2=" + result);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        for (MyDevice device : myDevices) {
        if (mGattUpdateReceiver != null)
            unregisterReceiver(mGattUpdateReceiver);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (ServiceConnection serviceConnection : serviceConnections) {
            unbindService(serviceConnection);
            for (MyDevice device : myDevices) {
                device.setBluetoothLeService(null);
                device.setBluetoothLeServiceTwo(null);
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.gatt_services, menu);
//        if (mConnected) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.menu_connect:
//                if(DEVICE_ONE != "")
//                    myDevices.get(0).getBluetoothLeService().connect(myDevices.get(0).getAddress());
//                return true;
//            case R.id.menu_disconnect:
//                if(DEVICE_ONE != "")
//                    myDevices.get(0).getBluetoothLeService().disconnect();
//                return true;
//            case R.id.menu_connect2:
//                if(DEVICE_TWO != "")
//                    myDevices.get(1).getBluetoothLeService().connect(myDevices.get(1).getAddress());
//                return true;
//            case R.id.menu_disconnect2:
//                if(DEVICE_TWO != "")
//                    myDevices.get(1).getBluetoothLeService().disconnect();
//                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String deviceAddress = intent.getStringExtra(BluetoothLeService.DEVICE_ADDRESS);
            for (MyDevice device : myDevices) {
                BluetoothLeService mBluetoothLeService = device.getBluetoothLeService();
                BluetoothLeServiceTwo mBluetoothLeServiceTwo = device.getBluetoothLeServiceTwo();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                    if (DEVICE_ONE.equalsIgnoreCase(deviceAddress)) {
                        mConnected = true;
                        updateConnectionState(R.string.connected);
                    }
                    if (DEVICE_TWO.equalsIgnoreCase(deviceAddress)) {
                        mConnected2 = true;
                        updateConnectionState2(R.string.connected);
                    }
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    invalidateOptionsMenu();
                    if (DEVICE_ONE.equalsIgnoreCase(deviceAddress)) {
                        mConnected = false;
                        clearUI();
                        updateConnectionState(R.string.disconnected);
                    }
                    if (DEVICE_TWO.equalsIgnoreCase(deviceAddress)) {
                        mConnected2 = false;
                        clearUI2();
                        updateConnectionState2(R.string.disconnected);
                    }
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    // Show all the supported services and characteristics on the user interface.
                    if (DEVICE_ONE.equalsIgnoreCase(deviceAddress) && mBluetoothLeService != null) {
                        displayGattServices(device, mBluetoothLeService.getSupportedGattServices());
                    }
                    if (DEVICE_TWO.equalsIgnoreCase(deviceAddress) && mBluetoothLeServiceTwo != null) {
                        displayGattServices(device, mBluetoothLeServiceTwo.getSupportedGattServices());
                    }
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    if (DEVICE_ONE.equalsIgnoreCase(deviceAddress)) {
                        displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    }
                    if (DEVICE_TWO.equalsIgnoreCase(deviceAddress)) {
                        displayData2(intent.getStringExtra(BluetoothLeServiceTwo.EXTRA_DATA));
                    }

                }
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
//    private final ExpandableListView.OnChildClickListener servicesListClickListner =
//            new ExpandableListView.OnChildClickListener() {
//                @Override
//                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
//                                            int childPosition, long id) {
//                    if (mGattCharacteristics.size() > 0) {
//
//                        final BluetoothGattCharacteristic characteristic =
//                                mGattCharacteristics.get(groupPosition).get(childPosition);
//                        final int charaProp = characteristic.getProperties();
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            // If there is an active notification on a characteristic, clear
//                            // it first so it doesn't update the data field on the user interface.
//                            if (mNotifyCharacteristic != null) {
//                                myDevices.get(0).getBluetoothLeService().setCharacteristicNotification(
//                                        mNotifyCharacteristic, false);
//                                mNotifyCharacteristic = null;
//                            }
//                            myDevices.get(0).getBluetoothLeService().readCharacteristic(characteristic);
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            mNotifyCharacteristic = characteristic;
//                            myDevices.get(0).getBluetoothLeService().setCharacteristicNotification(
//                                    characteristic, true);
//                        }
//                        return true;
//                    }
//                    return false;
//                }
//            };


    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
//    private final ExpandableListView.OnChildClickListener servicesListClickListner2 =
//            new ExpandableListView.OnChildClickListener() {
//                @Override
//                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
//                                            int childPosition, long id) {
//                    if (mGattCharacteristics2.size() > 0) {
//                        final BluetoothGattCharacteristic characteristic =
//                                mGattCharacteristics2.get(groupPosition).get(childPosition);
//                        final int charaProp = characteristic.getProperties();
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            // If there is an active notification on a characteristic, clear
//                            // it first so it doesn't update the data field on the user interface.
//                            if (mNotifyCharacteristic2 != null) {
//                                myDevices.get(1).getBluetoothLeServiceTwo().setCharacteristicNotification(
//                                        mNotifyCharacteristic2, false);
//                                mNotifyCharacteristic2 = null;
//                            }
//                            myDevices.get(1).getBluetoothLeServiceTwo().readCharacteristic(characteristic);
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            mNotifyCharacteristic2 = characteristic;
//                            myDevices.get(1).getBluetoothLeServiceTwo().setCharacteristicNotification(
//                                    characteristic, true);
//                        }
//                        return true;
//                    }
//                    return false;
//                }
//            };

    private void clearUI() {
//        binding.gattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        binding.dataValue.setText(R.string.no_data);
    }

    private void clearUI2() {
//        binding.gattServicesList2.setAdapter((SimpleExpandableListAdapter) null);
        binding.dataValue2.setText(R.string.no_data);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.connectionState.setText(resourceId);
                binding.buttonConnect.setText(resourceId);
            }
        });
    }

    private void updateConnectionState2(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.connectionState2.setText(resourceId);
                binding.buttonConnect2.setText(resourceId);
            }
        });
    }

//    String testData1 = "A1:ax,ay,az;G1:gx,gy,gz,t;F1:fsr_reading1,fsr_reading2,fsr_reading3,fsr_reading4,fsr_reading5,fsr_reading6,fsr_reading7;sadf";
//    String testData2 = "A2:ax,ay,az;G2:gx,gy,gz,t;F2:fsr_reading1,fsr_reading2,fsr_reading3,fsr_reading4,fsr_reading5,fsr_reading6,fsr_reading7;asdf";

    private void displayData(String rawData) {
        String[] dataArray = rawData.split(";");
//        String[] dataArray = Arrays.copyOf(arr, arr.length - 1);

        String processedData = "";
        for (String s : dataArray) {
            String[] sensorData = s.split(":");
            if (sensorData[0].contains("A1") || sensorData[0].contains("A2")) {
                String[] data = sensorData[1].split(",");
                processedData = processedData + "Accelerometer Data:\nX: " + data[0] + " Y: " + data[1] + "\nZ: " + data[2] + "\n";
            } else if (sensorData[0].contains("G1") || sensorData[0].contains("G2")) {
                String[] data = sensorData[1].split(",");
                processedData = processedData + "Gyroscope Data:\nX: " + data[0] + " Y: " + data[1] + "\nZ: " + data[2] + " T: " + data[3] + "\n";
            } else if (sensorData[0].contains("F1") || sensorData[0].contains("F2")) {
                String[] data = sensorData[1].split(",");
                processedData = processedData + "FSR Data:\n";
                int i=1;
                for (String f : data) {
                    processedData = processedData + " F"+i+": " + f;
                    i++;
                    if (i%2==0)
                        processedData = processedData + "\n";
                }
                processedData = processedData + "\n";
            }
            processedData = processedData + "\n";
        }
        if (processedData != null) {
            binding.dataValue.setText(processedData);
        }
    }

    private void displayData2(String rawData) {
        String[] dataArray = rawData.split(";");

        String processedData = "";
        for (String s : dataArray) {
            String[] sensorData = s.split(":");
            if (sensorData[0].contains("A1") || sensorData[0].contains("A2")) {
                String[] data = sensorData[1].split(",");
                processedData = processedData + "Accelerometer Data:\nX: " + data[0] + " Y: " + data[1] + "\nZ: " + data[2] + "\n";
            } else if (sensorData[0].contains("G1") || sensorData[0].contains("G2")) {
                String[] data = sensorData[1].split(",");
                processedData = processedData + "Gyroscope Data:\nX: " + data[0] + " Y: " + data[1] + "\nZ: " + data[2] + " T: " + data[3] + "\n";
            } else if (sensorData[0].contains("F1") || sensorData[0].contains("F2")) {
                String[] data = sensorData[1].split(",");
                processedData = processedData + "FSR Data:\n";
                int i=1;
                for (String f : data) {
                    processedData = processedData + " F"+i+": " + f;
                    i++;
                    if (i%2==0)
                        processedData = processedData + "\n";
                }
                processedData = processedData + "\n";
            }
            processedData = processedData + "\n";
        }
        if (processedData != null) {
            binding.dataValue2.setText(processedData);
        }
    }

    private ServiceConnection initService(MyDevice myDevice) {
        ServiceConnection mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                if (DEVICE_ONE.equalsIgnoreCase(myDevice.getAddress())) {
                    myDevice.setBluetoothLeService(((BluetoothLeService.LocalBinder) service).getService());
                    //updating myDevice object with new bluetoothLeService

                    if (!myDevice.getBluetoothLeService().initialize()) {
                        Log.e(TAG, "Unable to initialize Bluetooth");
                        finish();
                    }
                    // Automatically connects to the device upon successful start-up initialization.
                    myDevice.getBluetoothLeService().connect(myDevice.getAddress());
                }
                if (DEVICE_TWO.equalsIgnoreCase(myDevice.getAddress())) {
                    myDevice.setBluetoothLeServiceTwo(((BluetoothLeServiceTwo.LocalBinder) service).getService());
                    //updating myDevice object with new bluetoothLeService

                    if (!myDevice.getBluetoothLeServiceTwo().initialize()) {
                        Log.e(TAG, "Unable to initialize Bluetooth");
                        finish();
                    }
                    // Automatically connects to the device upon successful start-up initialization.
                    myDevice.getBluetoothLeServiceTwo().connect(myDevice.getAddress());
                }


            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

                myDevice.setBluetoothLeService(null);
                myDevice.setBluetoothLeServiceTwo(null);
            }
        };
        return mServiceConnection;
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(MyDevice device, List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        if (DEVICE_ONE.equalsIgnoreCase(device.getAddress())) {
            mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        }
        if (DEVICE_TWO.equalsIgnoreCase(device.getAddress())) {
            mGattCharacteristics2 = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        }
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            if (DEVICE_ONE.equalsIgnoreCase(device.getAddress())) {
                mGattCharacteristics.add(charas);
            }
            if (DEVICE_ONE.equalsIgnoreCase(device.getAddress())) {
                mGattCharacteristics2.add(charas);
            }
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

//        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
//                this,
//                gattServiceData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[]{LIST_NAME, LIST_UUID},
//                new int[]{android.R.id.text1, android.R.id.text2},
//                gattCharacteristicData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[]{LIST_NAME, LIST_UUID},
//                new int[]{android.R.id.text1, android.R.id.text2}
//        );
//        if (DEVICE_ONE.equalsIgnoreCase(device.getAddress())) {
//            binding.gattServicesList.setAdapter(gattServiceAdapter);
//        }
//        if (DEVICE_TWO.equalsIgnoreCase(device.getAddress())) {
//            binding.gattServicesList2.setAdapter(gattServiceAdapter);
//        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
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
}
