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
import com.example.bluetooth.Utils;

import com.example.bluetooth.vunit.UnitCase;
import com.example.bluetooth.vunit.UnitResult;
import com.example.bluetooth.vunit.UnitRunner;
import com.example.bluetooth.vunit.cases.TurnOffBT;
import com.example.bluetooth.vunit.cases.TurnOnBT;
public class EnableBluetoothAutoConnectActivity extends TemplateVunitTestActivity {
    public static final String TAG = "EnableBluetoothAutoConnectActivity";
    private static final List<String[]> mDefaultSettingsList = new ArrayList<String[]>() {{
        add(new String[]{"t", "测试次数", "" + 1000,
                SETTINGS_KEY_TYPE_VAL_STR});
        add(new String[]{"i", "测试间隔/ms", "" + 2000,
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
        super.startTest();
        updateInforText("正在测试第0项");
    }
    protected void stopTest() {
        //super.stopTest();
        updateInforText("测试已停止");
    }

    protected JSONObject getConfig() {
        JSONObject jsonObject = new JSONObject();
        Utils.putJsonObject(jsonObject, UnitCase.CONF_KEY_UIDS, UnitRunner.UID);
        String testTimes = Utils.getJsonObjectValFromJsonArray(mSettings,
                TemplateVunitTestActivity.SETTINGS_KEY_KEY, UnitRunner.CONF_KEY_TEST_TIMES,
                TemplateVunitTestActivity.SETTINGS_KEY_VALUE);
        
        if (null != testTimes) {
            Utils.putJsonObject(jsonObject, UnitRunner.CONF_KEY_TEST_TIMES, testTimes);
        }
        String testInterval = Utils.getJsonObjectValFromJsonArray(mSettings,
                TemplateVunitTestActivity.SETTINGS_KEY_KEY, UnitRunner.CONF_KEY_TEST_INTERVAL,
                TemplateVunitTestActivity.SETTINGS_KEY_VALUE);
        if (null != testInterval) {
            Utils.putJsonObject(jsonObject, UnitRunner.CONF_KEY_TEST_INTERVAL, testInterval);
        }
        {
            JSONArray jsonArray = new JSONArray();
            {
                JSONObject jsonObject1 = new JSONObject();
                Utils.putJsonObject(jsonObject1, UnitCase.CONF_KEY_UIDS, TurnOffBT.UID);
                jsonArray.put(jsonObject1);
            }
            {
                JSONObject jsonObject1 = new JSONObject();
                Utils.putJsonObject(jsonObject1, UnitCase.CONF_KEY_UIDS, TurnOnBT.UID);
                jsonArray.put(jsonObject1);
            }

            // 检查连接
       //     {
       //         JSONObject jsonObject1 = new JSONObject();
       //         Utils.putJsonObject(jsonObject1, UnitCase.CONF_KEY_UIDS, CheckConnectedCase.UID);
       //         Utils.putJsonObject(jsonObject1, CheckConnectedCase.CONF_KEY_ADDR,
       //                 Utils.getJsonObjectValFromJsonArray(mSettings, SETTINGS_KEY_KEY,
       //                         CONF_KEY_ADDR, SETTINGS_KEY_VALUE));
       //         jsonArray.put(jsonObject1);
       //     }
            Utils.putJsonObject(jsonObject, UnitRunner.CONF_KEY_VAL, jsonArray);
        }
        Log.v(TAG, "getConfig: " + jsonObject.toString());

        return jsonObject;
    }
}
