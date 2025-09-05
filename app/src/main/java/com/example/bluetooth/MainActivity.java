package com.example.bluetooth;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import com.example.bluetooth.vunit.acitivity.EnableBluetoothAutoConnectActivity;

public class MainActivity extends Activity {


    private Button btnSender = null;
    private Button btnReceiver  = null;
    private OnClickListener listener = new OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btn_start_sender_activity) {
                startSender();

            } else if (id == R.id.btn_start_receiver_activity){
                startReceiver();
            }
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSender = (Button)findViewById(R.id.btn_start_sender_activity);
        btnReceiver  = (Button)findViewById(R.id.btn_start_receiver_activity);
        btnSender.setOnClickListener(listener);
        btnReceiver.setOnClickListener(listener);
    }
    private void startSender() {
        //Intent senderIntent = new Intent(this, StartPairActivity.class);
        Intent senderIntent = new Intent(this, DeviceListActivity.class);
        startActivity(senderIntent);
    }
    private void startReceiver() {
        Intent receiverIntent = new Intent(this, EnableBluetoothAutoConnectActivity.class);
        //startActivity(receiverIntent);
        startActivityForResult(receiverIntent, 0);
    }
    protected void onDestroy() {
        super.onDestroy();
    }
}


