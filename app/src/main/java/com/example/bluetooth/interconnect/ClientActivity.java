package com.example.bluetooth.interconnect;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;
import com.example.bluetooth.R;
import android.content.Intent;
import android.widget.EditText;


public class ClientActivity extends AppCompatActivity {
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private TextView tvStatus, tvReceived;
    private Button btnSend, btnConnect, btnScan;
    private static final int REQUEST_SCAN = 3;
    private EditText editText;
    BluetoothDevice selectedDevice = null;
    
    // BLE服务UUID和特征UUID
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00001102-0000-1000-8000-00805F9B34FB");
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        
        initializeViews();
        setupBluetooth();
        setupClickListeners();
    }
    
    private void initializeViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvReceived = findViewById(R.id.tv_received);
        btnSend = findViewById(R.id.btn_send);
        btnConnect = findViewById(R.id.btn_connect);
        btnScan = findViewById(R.id.btn_scan);
        editText = findViewById(R.id.editText);
        
        btnSend.setEnabled(false);
    }
    
    private void setupBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            tvStatus.setText("蓝牙未开启");
            return;
        }
        tvStatus.setText("蓝牙已就绪");
    }
    
    private void setupClickListeners() {
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(ClientActivity.this, ScannerActivity.class);
            //startActivity(intent);

            startActivityForResult(intent,REQUEST_SCAN);
        });

        btnConnect.setOnClickListener(v -> scanAndConnect());
        
        btnSend.setOnClickListener(v -> {
            String content = editText.getText().toString().trim();
            if (bluetoothGatt != null) {
                sendData(content);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
       if (requestCode == REQUEST_SCAN) {
            if (resultCode == RESULT_OK) {
                selectedDevice = data.getParcelableExtra("SELECTED_DEVICE");
            }
        }
    }
    
    private void scanAndConnect() {
        tvStatus.setText("正在连接设备...");
        
        // 模拟扫描到设备并连接
        handler.postDelayed(() -> {
            BluetoothDevice device = selectedDevice; 
            bluetoothGatt = device.connectGatt(this, false, gattCallback);
            tvStatus.setText("正在连接服务端...");
        }, 2000);
    }
    
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            runOnUiThread(() -> {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    tvStatus.setText("已连接服务端");
                    btnSend.setEnabled(true);
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    tvStatus.setText("连接断开");
                    btnSend.setEnabled(false);
                }
            });
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
                gatt.setCharacteristicNotification(characteristic, true);
                runOnUiThread(() -> tvStatus.setText("服务发现完成，可以通信"));
            }
        }
        
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            runOnUiThread(() -> tvStatus.setText("数据发送成功"));
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final String receivedData = new String(characteristic.getValue());
            Log.e("txl","./java/com/example/bluetooth/interconnect/ClientActivity.java:139 tangxinlou debug 9" +  new Object(){}.getClass().getEnclosingMethod().getName() + receivedData);
            runOnUiThread(() -> tvReceived.setText("收到: " + receivedData));
        }
    };
    
    private void sendData(String data) {
        if (bluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = 
                bluetoothGatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
            characteristic.setValue(data);
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}

