package com.example.bluetooth.vunit.acitivity;
import android.bluetooth.BluetoothHearingAid;
import android.os.Bundle;
import android.util.Log;
import com.example.bluetooth.R;
import com.example.bluetooth.TemplateVunitTestActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public class EnableBluetoothAutoConnectActivity extends TemplateVunitTestActivity {
    public static final String TAG = "EnableBluetoothAutoConnectActivity";
    private static final List<String[]> mDefaultSettingsList = new ArrayList<String[]>() {{
        add(new String[]{"k", "测试次数", "" + 1000,
                SETTINGS_KEY_TYPE_VAL_STR});
        add(new String[]{"v", "测试间隔/ms", "" + 2000,
                SETTINGS_KEY_TYPE_VAL_STR});
        add(new String[]{"a", "测试设备", "00:00:00:00:00:00",
                SETTINGS_KEY_TYPE_VAL_ADDR});
    }};
    private static final String CONF_KEY_ADDR = "a";
    public EnableBluetoothAutoConnectActivity() {
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateInforText("等待测试");
    }
    protected void onStart() {
        super.onStart();
    }
    protected JSONArray getSettingsDefaultJson() {
        return getJsonArrayFromList(mDefaultSettingsList);
    }
    protected void startTest() {
        //super.startTest();
        updateInforText("正在测试第0项");
    }
    protected void stopTest() {
        //super.stopTest();
        updateInforText("测试已停止");
    }
}
