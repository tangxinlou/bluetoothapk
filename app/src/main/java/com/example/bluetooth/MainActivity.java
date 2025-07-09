package com.example.bluetooth;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends Activity {


    private Button btnSender = null;
    private OnClickListener listener = new OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btn_start_sender_activity) {
                startSender();

            }
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSender = (Button)findViewById(R.id.btn_start_sender_activity);
        btnSender.setOnClickListener(listener);
    }
    private void startSender() {
        //Intent senderIntent = new Intent(this, StartPairActivity.class);
        Intent senderIntent = new Intent(this, DeviceListActivity.class);
        startActivity(senderIntent);
    }
    protected void onDestroy() {
        super.onDestroy();
    }
}


