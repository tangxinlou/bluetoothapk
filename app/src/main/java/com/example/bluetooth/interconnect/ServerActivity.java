package com.example.bluetooth.interconnect;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;
import com.example.bluetooth.R;
import android.widget.EditText;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

public class ServerActivity extends AppCompatActivity {
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothManager bluetoothManager;
    private TextView tvStatus, tvReceived;
    private Button btnSend, btnStart;
    public Advertiser mAdvertiser ;
    private EditText editText;
    private BluetoothGattCharacteristic characteristic;
    private static final String TAG = "ServerActivity";
    
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00001102-0000-1000-8000-00805F9B34FB");
    
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
         mAdvertiser  = new Advertiser(this);
        initializeViews();
        setupBluetooth();
        setupClickListeners();
    }
    
    private void initializeViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvReceived = findViewById(R.id.tv_received);
        btnSend = findViewById(R.id.btn_send);
        btnStart = findViewById(R.id.btn_start);
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
        btnStart.setOnClickListener(v -> startBLEService());
        
        btnSend.setOnClickListener(v -> {
            String content = editText.getText().toString().trim();
            if (bluetoothGattServer != null) {
                sendData(content);
            }
        });
    }
    
    private void startBLEService() {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback);
        
        // 创建BLE服务
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, 
            BluetoothGattService.SERVICE_TYPE_PRIMARY);
        
        // 创建特征
        characteristic = new BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | 
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        
        service.addCharacteristic(characteristic);
        bluetoothGattServer.addService(service);
        
        tvStatus.setText("服务端已启动，等待连接...");
        btnSend.setEnabled(true);
        mAdvertiser.startAdvertising(); 
    }
    
    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(android.bluetooth.BluetoothDevice device, int status, int newState) {
            runOnUiThread(() -> {
                if (newState == BluetoothGattServer.STATE_CONNECTED) {
                    tvStatus.setText("客户端已连接");
                } else if (newState == BluetoothGattServer.STATE_DISCONNECTED) {
                    tvStatus.setText("客户端断开连接");
                }
            });
        }
        
        @Override
        public void onCharacteristicReadRequest(android.bluetooth.BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }
        
        @Override
        public void onCharacteristicWriteRequest(android.bluetooth.BluetoothDevice device, int requestId, 
            BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, 
            int offset, byte[] value) {
            
            final String receivedData = new String(value);
            runOnUiThread(() -> tvReceived.setText("收到: " + receivedData));
            
            if (responseNeeded) {
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }
        }
    };
    
    private void sendData(String data) {
        characteristic.setValue(data);

        // 通知所有连接的客户端
        Log.e(TAG,"./java/com/example/bluetooth/interconnect/ServerActivity.java:141 tangxinlou debug 7" +  new Object(){}.getClass().getEnclosingMethod().getName());
        for (BluetoothDevice device : bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER)) {

            Log.e(TAG,"./java/com/example/bluetooth/interconnect/ServerActivity.java:143 tangxinlou debug 6" +  new Object(){}.getClass().getEnclosingMethod().getName() + device);
            boolean success = bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
            if (success) {
                Log.d(TAG, "数据发送成功，长度：" + data.length());
            } else {
                Log.e(TAG, "数据发送失败");
            }
        }
        Log.e(TAG,"./java/com/example/bluetooth/interconnect/ServerActivity.java:152 tangxinlou debug 8" +  new Object(){}.getClass().getEnclosingMethod().getName());
        tvStatus.setText("发送数据: " + data);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGattServer != null) {
            bluetoothGattServer.close();
            bluetoothGattServer = null;
        }
    }
}

