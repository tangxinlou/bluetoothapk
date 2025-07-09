package com.example.bluetooth;
import android.os.Bundle;
import android.app.Activity;

public class SecondActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
