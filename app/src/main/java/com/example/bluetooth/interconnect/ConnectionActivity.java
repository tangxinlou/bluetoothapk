package com.example.bluetooth.interconnect;
import android.util.Log;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bluetooth.R;

public class ConnectionActivity extends AppCompatActivity {
    
    private BluetoothGatt bluetoothGatt;
    private boolean connected = false;
    private TextView statusText;
    private TextView servicesText;
    private TextView dataText;
    private Button disconnectButton;
    private Button readDataButton;
    
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                connected = true;
                runOnUiThread(() -> statusText.setText("已连接到设备"));
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            connected = false;
            runOnUiThread(() -> statusText.setText("设备已断开连接"));
            }
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread(() -> {
                    statusText.setText("服务发现完成");
                    displayServices(gatt.getServices());
                });
            }
        }
        
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread(() -> {
                    statusText.setText("特征值读取成功");
                    byte[] value = characteristic.getValue();
                    if (value != null && value.length > 0) {
                        String stringValue = new String(value);
                        dataText.append("特征值: " + stringValue + "\n");
                    }
                });
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        
        initializeViews();
        setupClickListeners();
        
        BluetoothDevice selectedDevice = getIntent().getParcelableExtra("scan_device");
        if (selectedDevice != null) {
            connectToDevice(selectedDevice);
            statusText.setText(selectedDevice.getAddress());
        } else {
            statusText.setText("未选择设备");
        }
    }
    
    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        servicesText = findViewById(R.id.servicesText);
        dataText = findViewById(R.id.dataText);
        disconnectButton = findViewById(R.id.disconnectButton);
        readDataButton = findViewById(R.id.readDataButton);
    }
    
    private void setupClickListeners() {
        disconnectButton.setOnClickListener(v -> {
            disconnectFromDevice();
        });
        
        readDataButton.setOnClickListener(v -> {
            readCharacteristic();
        });
    }
    
    private void connectToDevice(BluetoothDevice device) {
        statusText.setText("正在连接设备: " + (device.getName() != null ? device.getName() : "未知设备"));
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }
    
    private void disconnectFromDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        connected = false;
        statusText.setText("已断开连接");
    }
    
    private void displayServices(java.util.List<BluetoothGattService> services) {
        servicesText.setText("发现的服务:\n");
        for (BluetoothGattService service : services) {
            servicesText.append("服务: " + service.getUuid() + "\n");
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            servicesText.append("  特征: " + characteristic.getUuid() + "\n");
            }
        }
    }
    
    private void readCharacteristic() {
        if (bluetoothGatt != null && !bluetoothGatt.getServices().isEmpty()) {
            BluetoothGattService service = bluetoothGatt.getServices().get(0);
            if (!service.getCharacteristics().isEmpty()) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(0);
                bluetoothGatt.readCharacteristic(characteristic);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromDevice();
    }
}
