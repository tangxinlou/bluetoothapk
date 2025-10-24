package com.example.bluetooth.interconnect;
import android.util.Log;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.bluetooth.R;
import android.bluetooth.BluetoothDevice;

public class InterConnectActivity  extends AppCompatActivity {
    
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_SCAN = 3;
    
    private BluetoothAdapter bluetoothAdapter;
    private TextView statusText;
    private Button startScanButton;
    private Button startBroadcastButton;
    private Button connectButton;
    private Button btnClient, btnServer;
    BluetoothDevice selectedDevice = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interconnect);
        
        initializeViews();
        initializeBluetooth();
        setupClickListeners();
    }
    
    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        startScanButton = findViewById(R.id.startScanButton);
        startBroadcastButton = findViewById(R.id.startBroadcastButton);
        connectButton = findViewById(R.id.connectButton);
        btnClient = findViewById(R.id.btn_client);
        btnServer = findViewById(R.id.btn_server);
    }
    
    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        
        if (bluetoothAdapter == null) {
            statusText.setText("设备不支持蓝牙");
            disableButtons();
            return;
        }
        
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            checkPermissions();
        }
    }
    
    private void checkPermissions() {
        String[] requiredPermissions;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions = new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            requiredPermissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        
        if (hasPermissions(requiredPermissions)) {
            statusText.setText("蓝牙已准备就绪");
            enableButtons();
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    private void setupClickListeners() {
        startScanButton.setOnClickListener(v -> {
            Intent intent = new Intent(InterConnectActivity.this, ScannerActivity.class);
            //startActivity(intent);

            startActivityForResult(intent,REQUEST_SCAN);
        });
        
        startBroadcastButton.setOnClickListener(v -> {
            Intent intent = new Intent(InterConnectActivity.this, BroadcasterActivity.class);
            startActivity(intent);
        });
        
        connectButton.setOnClickListener(v -> {
            Intent intent = new Intent(InterConnectActivity.this, ConnectionActivity.class);
            intent.putExtra("scan_device", selectedDevice);
            startActivity(intent);
        });
        btnClient.setOnClickListener(v -> {
            Intent intent = new Intent(InterConnectActivity.this, ClientActivity.class);
            intent.putExtra("scan_device", selectedDevice);
            startActivity(intent);
        });

        btnServer.setOnClickListener(v -> {
            Intent intent = new Intent(InterConnectActivity.this, ServerActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                statusText.setText("蓝牙已启用");
                checkPermissions();
            } else {
                statusText.setText("蓝牙启用失败");
                disableButtons();
            }
        }
        if (requestCode == REQUEST_SCAN) {
            if (resultCode == RESULT_OK) {
                selectedDevice = data.getParcelableExtra("SELECTED_DEVICE");
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                statusText.setText("权限已授予，蓝牙准备就绪");
                enableButtons();
            } else {
                statusText.setText("权限被拒绝，无法使用蓝牙功能");
                disableButtons();
            }
        }
    }
    
    private void enableButtons() {
        startScanButton.setEnabled(true);
        startBroadcastButton.setEnabled(true);
        connectButton.setEnabled(true);
    }
    
    private void disableButtons() {
        startScanButton.setEnabled(false);
        startBroadcastButton.setEnabled(false);
        connectButton.setEnabled(false);
    }
}
