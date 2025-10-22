package com.example.bluetooth.interconnect;
import android.util.Log;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import java.util.UUID;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanFilter;
import android.os.ParcelUuid;
import com.example.bluetooth.R;





public class ScannerActivity extends AppCompatActivity {
    
    private static final long SCAN_PERIOD = 10000;
    
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    private Handler handler = new Handler();
    
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private DeviceAdapter deviceAdapter;
    private TextView statusText;
    private Button startScanButton;
    private Button stopScanButton;
    private RecyclerView devicesRecyclerView;
    private static final UUID UUID_STEPFUN_SERVICE = UUID.fromString("00001807-0000-1000-8000-00805f9b34fb");
    
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (!containsDevice(device)) {
                deviceList.add(device);
                deviceAdapter.notifyDataSetChanged();
                statusText.setText("发现设备: " + (device.getName() != null ? device.getName() : "未知设备") + " - " + device.getAddress());
            }
        }
        
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                BluetoothDevice device = result.getDevice();
                if (!containsDevice(device)) {
                    deviceList.add(device);
                }
            }
            deviceAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            statusText.setText("扫描失败: " + errorCode);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        
        initializeViews();
        initializeScanner();
        setupRecyclerView();
        setupClickListeners();
    }
    
    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        startScanButton = findViewById(R.id.startScanButton);
        stopScanButton = findViewById(R.id.stopScanButton);
        devicesRecyclerView = findViewById(R.id.devicesRecyclerView);
    }
    
    private void initializeScanner() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        statusText.setText("准备开始扫描BLE设备");
    }
    
    private void setupRecyclerView() {
        deviceAdapter = new DeviceAdapter(deviceList, device -> {
            Intent intent = new Intent();
            intent.putExtra("SELECTED_DEVICE", device);
            setResult(RESULT_OK, intent);
            finish();
        });
        
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesRecyclerView.setAdapter(deviceAdapter);
    }
    
    private void setupClickListeners() {
        startScanButton.setOnClickListener(v -> {
            if (!scanning) {
                startScan();
            } else {
                stopScan();
            }
        });
        
        stopScanButton.setOnClickListener(v -> {
            stopScan();
        });
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();

        // 设置扫描模式 - 平衡功耗和扫描速度
        builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);

        // 设置回调类型 - 主动扫描获取更多设备信息
        builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);

        // 设置匹配模式 - 积极匹配提高发现率
        builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);

        // 设置扫描结果报告延迟
        builder.setReportDelay(0); // 0表示立即报告

        // 设置扫描数量限制
        builder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);

        return builder.build();
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> filters = new ArrayList<>();

        // 过滤特定服务UUID的设备
        ScanFilter serviceFilter = new ScanFilter.Builder()
            .setServiceUuid(new ParcelUuid(UUID_STEPFUN_SERVICE))
            .build();
        filters.add(serviceFilter);
        return filters;
    }

    private void startScan() {
        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();
        
        scanning = true;
        startScanButton.setText("停止扫描");
        statusText.setText("正在扫描BLE设备...");
        ScanSettings scanSettings = buildScanSettings();
        List<ScanFilter> scanFilters = buildScanFilters();
        
        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        
        handler.postDelayed(this::stopScan, SCAN_PERIOD);
    }
    
    private void stopScan() {
        scanning = false;
        startScanButton.setText("开始扫描");
        statusText.setText("扫描已停止，发现 " + deviceList.size() + " 个设备");
        bluetoothLeScanner.stopScan(scanCallback);
    }
    
    private boolean containsDevice(BluetoothDevice device) {
        for (BluetoothDevice d : deviceList) {
            if (d.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanning) {
            stopScan();
        }
    }
}
