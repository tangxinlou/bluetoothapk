package com.example.bluetooth;
import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.content.Intent;
import com.example.bluetooth.interconnect.InterConnectActivity;
import com.example.bluetooth.nrf.NrfActivity;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TabWidget;

public class BluetoothTestTab extends TabActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabhost);
        TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec(
                getString(R.string.base_tab)).setIndicator(
                getString(R.string.base_tab)).setContent(
                new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)));
        tabHost.addTab(tabHost.newTabSpec(
                getString(R.string.case_tab)).setIndicator(
                getString(R.string.case_tab)).setContent(
                new Intent(this, SecondActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)));
        tabHost.addTab(tabHost.newTabSpec(
                getString(R.string.list_tab)).setIndicator(
                getString(R.string.list_tab)).setContent(
                new Intent(this, ThirdActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)));
        tabHost.addTab(tabHost.newTabSpec(
                getString(R.string.interconnect_tab)).setIndicator(
                getString(R.string.interconnect_tab)).setContent(
                new Intent(this, InterConnectActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)));
        tabHost.setCurrentTab(0);
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
