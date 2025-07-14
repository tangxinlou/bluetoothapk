package com.example.bluetooth;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class Utils {
    private static final String TAG = "Utils";
    public static final String EXTRA_LAUNCH_BLE_DEVICE = "extra_launch_ble_device";
    public static JSONObject getJsonObjectFromJsonArray(JSONArray jsonArray, int index) {
        JSONObject jsonObject = null;
        try {
            jsonObject = jsonArray.getJSONObject(index);
        } catch (JSONException e) {
            Log.w(TAG, "getJsonObjectFromJsonArray with err", e);
        }
        return jsonObject;
    }
    public static void putJsonObject(JSONObject jsonObject, String key, Object object) {
        try {
            jsonObject.put(key, object);
        } catch (JSONException e) {
            Log.wtf(TAG, "putJsonObject with err", e);
        }
    }
    public static JSONArray getJsonArrayFromString(String str) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(str);
        } catch (JSONException e) {
            Log.w(TAG, "getJsonArrayFromString with err", e);
        }
        return jsonArray;
    }
}
