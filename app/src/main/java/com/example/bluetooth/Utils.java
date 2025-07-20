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
    public static JSONObject getJsonObjectFromJsonArray(JSONArray jsonArray, String key,
                                                        Object val) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject1 = getJsonObjectFromJsonArray(jsonArray, i);
            Object val1 = jsonObject1.opt(key);
            if (val == val1) return jsonObject1;
            if (val != null && val.equals(val1)) return jsonObject1;
        }
        return null;
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
    public static String getJsonObjectValFromJsonArray(JSONArray jsonArray, String jObjKey,
                                                        Object jObjVal, String valKey) {
        JSONObject jsonObject = getJsonObjectFromJsonArray(jsonArray, jObjKey, jObjVal);
        if (null == jsonObject) return null;
        return jsonObject.optString(valKey, null);
    }
    public static String logLevelToString(int level) {
        switch (level) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            case Log.ASSERT:
                return "ASSERT";
            default:
                return "U";
        }
    }
}
