package com.example.bluetooth.interconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.example.bluetooth.R;
import com.example.bluetooth.DeviceListActivity;

public class SocketConnect extends AppCompatActivity {
    
    private static final String TAG = "BluetoothChat";
    private static final UUID MY_UUID = UUID.fromString("00000837-0000-1000-8000-00805F9B34FB");
    private static final String APP_NAME = "BluetoothChat";
    
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> messageAdapter;
    private List<String> messageList;
    private ListView messageListView;
    private EditText inputEditText;
    private Button sendButton;
    private Button startServerButton;
    private Button discoverButton;
    private TextView statusTextView;
    
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        
        initViews();
        checkPermissions();
        initBluetooth();
    }
    
    private void initViews() {
        messageListView = findViewById(R.id.messageListView);
        inputEditText = findViewById(R.id.inputEditText);
        sendButton = findViewById(R.id.sendButton);
        startServerButton = findViewById(R.id.startServerButton);
        discoverButton = findViewById(R.id.discoverButton);
        statusTextView = findViewById(R.id.statusTextView);
        
        messageList = new ArrayList<>();
        messageAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(messageAdapter);
        
        sendButton.setOnClickListener(v -> sendMessage());
        startServerButton.setOnClickListener(v -> startServer());
        discoverButton.setOnClickListener(v -> discoverDevices());
    }
    
    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
        
        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION);
        }
    }
    
    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
        }
        
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            updateStatus("蓝牙已启用");
        }
    }
    
    private void startServer() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        acceptThread = new AcceptThread();
        acceptThread.start();
        updateStatus("服务端已启动，等待连接...");
    }
    
    private void discoverDevices() {
        Intent receiverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(receiverIntent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String selectedDevice = data.getStringExtra("SELECTED_DEVICE");
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(selectedDevice);
                    String deviceInfo = "发现设备: " + device.getName() + " - " + device.getAddress();
                    addMessage(deviceInfo);
                    
                    connectToDevice(device);
            }
        }
    }
    
    
    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        
        connectThread = new ConnectThread(device);
        connectThread.start();
        updateStatus("正在连接到: " + device.getName());
    }
    
    private void sendMessage() {
        String message = inputEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(message) && connectedThread != null) {
            connectedThread.write(message.getBytes());
            addMessage("我: " + message);
            inputEditText.setText("");
        }
    }
    
    private void addMessage(String message) {
        runOnUiThread(() -> {
            messageList.add(message);
            messageAdapter.notifyDataSetChanged();
            messageListView.smoothScrollToPosition(messageList.size() - 1);
        });
    }
    
    private void updateStatus(String status) {
        runOnUiThread(() -> statusTextView.setText(status));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (acceptThread != null) {
            acceptThread.cancel();
        }
        if (connectThread != null) {
            connectThread.cancel();
        }
        if (connectedThread != null) {
            connectedThread.cancel();
        }
    }
    
    // 服务端监听线程
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        
        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                //tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }
        
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                
                if (socket != null) {
                    manageConnectedSocket(socket);
                    break;
                }
            }
        }
        
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 客户端连接线程
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        
        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }
        
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            
            try {
                socket.connect();
                manageConnectedSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
        
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 已连接线程处理数据传输
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        
        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            inputStream = tmpIn;
            outputStream = tmpOut;
        }
        
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String receivedMessage = new String(buffer, 0, bytes);
                    addMessage("对方: " + receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void manageConnectedSocket(BluetoothSocket socket) {
        runOnUiThread(() -> {
            updateStatus("已连接到设备");
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        });
    }
}

