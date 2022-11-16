package com.example.android.bluetoothlegatt;

public class MyDevice {

    private String name;
    private String address;
    BluetoothLeService bluetoothLeService;
    BluetoothLeServiceTwo bluetoothLeServiceTwo;

    public BluetoothLeServiceTwo getBluetoothLeServiceTwo() {
        return bluetoothLeServiceTwo;
    }

    public void setBluetoothLeServiceTwo(BluetoothLeServiceTwo bluetoothLeServiceTwo) {
        this.bluetoothLeServiceTwo = bluetoothLeServiceTwo;
    }


    public BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    public MyDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
