package com.example.bluetooth.interconnect;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;
import android.os.ParcelUuid;
import com.example.bluetooth.R;


public class BroadcasterActivity extends AppCompatActivity {
    
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private boolean advertising = false;
    private TextView statusText;
    private Button startBroadcastButton;
    private Button stopBroadcastButton;
    private static final UUID UUID_STEPFUN_SERVICE = UUID.fromString("00001807-0000-1000-8000-00805f9b34fb");
    
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            advertising = true;
            statusText.setText("BLE广播已启动");
            startBroadcastButton.setText("停止广播");
            Toast.makeText(BroadcasterActivity.this, "BLE广播已成功启动", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            advertising = false;
            startBroadcastButton.setText("开始广播");
            statusText.setText("广播启动失败: " + errorCode);
            Toast.makeText(BroadcasterActivity.this, "BLE广播启动失败: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);
        
        initializeViews();
        initializeAdvertiser();
        setupClickListeners();
    }
    
    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        startBroadcastButton = findViewById(R.id.startBroadcastButton);
        stopBroadcastButton = findViewById(R.id.stopBroadcastButton);
    }
    
    private void initializeAdvertiser() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        statusText.setText("准备开始BLE广播");
    }
    
    private void setupClickListeners() {
        startBroadcastButton.setOnClickListener(v -> {
            if (!advertising) {
                startAdvertising();
            } else {
                stopAdvertising();
            }
        });
        
        stopBroadcastButton.setOnClickListener(v -> {
            stopAdvertising();
        });
    }
    
    private void startAdvertising() {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(true)
            .setTimeout(0)
            .build();
        
        AdvertiseData advertiseData = new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(new ParcelUuid(UUID_STEPFUN_SERVICE))
            .build();
        
        Log.e("txl","java/com/example/bluetooth/interconnect/BroadcasterActivity.java:95 tangxinlou debug 1" +  new Object(){}.getClass().getEnclosingMethod().getName());
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback);
    }
    
    private void stopAdvertising() {
        if (advertising) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            advertising = false;
            startBroadcastButton.setText("开始广播");
            statusText.setText("BLE广播已停止");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (advertising) {
            stopAdvertising();
        }
    }
}
