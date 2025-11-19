package com.example.bluetooth;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.Set;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.AdapterView;
import android.view.View;
import java.util.HashSet;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.util.Log;
import java.util.HashMap;
import android.widget.TextView;
public class DeviceListActivity extends Activity {
    private static final String TAG = "DeviceListActivity";
    public BluetoothAdapter mBtAdapter;
    public ArrayAdapter<String> mNewDevicesArrayAdapter;
    private Button scanbutton = null;
    private OnClickListener listener = new OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.button_scan) {
                DeviceListActivity.this.doDiscovery();
                v.setVisibility(8);
            }
        }
    };
    final HashMap<BluetoothDevice, String> mDeviceMap = new HashMap<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device.getBondState() != 12 && device.getName() != null && DeviceListActivity.this.mDeviceMap.get(device) == null) {
                    ArrayAdapter access$200 = DeviceListActivity.this.mNewDevicesArrayAdapter;
                    DeviceListActivity.this.mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());

//                    ArrayAdapter access$200 = DeviceListActivity.this.mNewDevicesArrayAdapter;
//                    access$200.add(device.getName() + "\n" + device.getAddress());
                    DeviceListActivity.this.mDeviceMap.put(device, device.getName());
                }
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                DeviceListActivity.this.setProgressBarIndeterminateVisibility(false);
                DeviceListActivity.this.setTitle("选择设备");
                Log.e(TAG,"./app/src/main/java/com/example/bluetooth/DeviceListActivity.java:56 tangxinlou debug 5" +  new Object(){}.getClass().getEnclosingMethod().getName());
                if (DeviceListActivity.this.mNewDevicesArrayAdapter.getCount() == 0) {
                    DeviceListActivity.this.mNewDevicesArrayAdapter.add("no device found");
                }
                DeviceListActivity.this.mNewDevicesArrayAdapter.add("扫描结束");
            }
        }
    };


    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3) {
            TextView v = (TextView) view.findViewById(R.id.device_name_text);
            DeviceListActivity.this.mBtAdapter.cancelDiscovery();
            String info = v.getText().toString();
            String address = info.substring(info.length() - 17);
            Log.i(TAG, "tangxinlou debug onItemClick() info=" + info + ", address=" + address);
            Log.i(TAG, "tangxinlou debug onItemClick() address=" + address);
            Intent intent = new Intent();
            intent.putExtra("SELECTED_DEVICE", address);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicelistactivity);



        scanbutton = (Button)findViewById(R.id.button_scan);
        scanbutton.setOnClickListener(listener);

        ArrayAdapter<String> pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name, R.id.device_name_text);
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setClickable (true);
        pairedListView.setOnItemClickListener(this.mDeviceClickListener);

        this.mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name, R.id.device_name_text);
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(this.mNewDevicesArrayAdapter);
        newDevicesListView.setClickable (true);
        newDevicesListView.setOnItemClickListener(this.mDeviceClickListener);

        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!this.mBtAdapter.isEnabled()) {
            this.mBtAdapter.enable();
        }
        final Set<BluetoothDevice> pairedDevices = this.mBtAdapter.getBondedDevices();
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED"));
        Set<BluetoothDevice> devices = new HashSet<>();
        {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
            }
        }
        if (devices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(0);
            for (BluetoothDevice device : devices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            return;
        }

        pairedDevicesArrayAdapter.add("无配对");
    }


    public void doDiscovery() {
        Log.d(TAG, "doDiscovery()");
        findViewById(R.id.title_new_devices).setVisibility(0);
        this.mBtAdapter.startDiscovery();

    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
