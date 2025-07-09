package com.example.bluetooth;
import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.content.Intent;

public class BluetoothTestTab extends TabActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(
                getString(R.string.base_tab)).setIndicator(
                getString(R.string.base_tab)).setContent(
                new Intent(this, MainActivity.class)));
        tabHost.addTab(tabHost.newTabSpec(
                getString(R.string.case_tab)).setIndicator(
                getString(R.string.case_tab)).setContent(
                new Intent(this, SecondActivity.class)));
        tabHost.addTab(tabHost.newTabSpec(
                getString(R.string.list_tab)).setIndicator(
                getString(R.string.list_tab)).setContent(
                new Intent(this, ThirdActivity.class)));
        tabHost.setCurrentTab(0);
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
