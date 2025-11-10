package com.example.bluetooth.nrf;
import android.util.Log;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import com.example.bluetooth.R;
import com.example.bluetooth.BluetoothTestTab;
import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

public class NrfActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private Spinner spinner;
    private Button leftdrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nrf);

        initViews();
        setupDrawerLayout();
        setupTabLayout();
        setupSpinner();
        setupButton();
    }
    private OnClickListener listener = new OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.drawer_button) {
                startLeftDrawer();
            }
        }
    };

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tabLayout = findViewById(R.id.tab_layout);
        spinner = findViewById(R.id.spinner);
        leftdrawer = findViewById(R.id.drawer_button);
        leftdrawer.setOnClickListener(listener);
    }

    private void setupDrawerLayout() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Toast.makeText(NrfActivity.this, "首页", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(NrfActivity.this, "个人资料", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_settings) {
                    Toast.makeText(NrfActivity.this, "设置", Toast.LENGTH_SHORT).show();
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("标签1"));
        tabLayout.addTab(tabLayout.newTab().setText("标签2"));
        tabLayout.addTab(tabLayout.newTab().setText("标签3"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(NrfActivity.this, "选中: " + tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSpinner() {
        String[] items = {"选项1", "选项2", "选项3", "选项4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private void startLeftDrawer() {
        drawerLayout.openDrawer(navigationView);

    }

    private void setupButton() {
        Button centerButton = findViewById(R.id.center_button);
        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NrfActivity.this, "中间按钮被点击", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
