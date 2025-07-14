package com.example.bluetooth;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanSettings;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
public class VunitTestSettingsActivity extends Activity {
    private static final String TAG = "VunitTestSettingsActivity";
    private Button mButtonSave;
    private Button mButtonBack;
    private ListView mListView;
    private JSONArray mJsonArray;
    private LayoutInflater mLayoutInflater;
    public VunitTestSettingsActivity() {
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vunit_test_settings);
        setTitle("设置");
        mLayoutInflater = LayoutInflater.from(this);
        mButtonSave = findViewById(R.id.button);
        mButtonBack = findViewById(R.id.button2);
        mListView = findViewById(R.id.list);
        mListView.setAdapter(baseAdapter);
        mButtonSave.setText("保存");
        mButtonBack.setText("取消");
        mButtonSave.setOnClickListener(mOnClickListener);
        mButtonBack.setOnClickListener(mOnClickListener);
        try {
            mJsonArray =
                    new JSONArray(getIntent().getStringExtra(TemplateVunitTestActivity.EXTRA_SETTINGS_STR));
        } catch (Exception e) {
            Log.e(TAG, "err parse json", e);
        }
        Log.i(TAG, "jsonArr=" + mJsonArray);
        Log.i(TAG, "jsonArrLen=" + mJsonArray.length());
        baseAdapter.notifyDataSetChanged();
    }
    private void saveAndFinish() {
        Intent intent = new Intent();
        intent.putExtra(TemplateVunitTestActivity.EXTRA_SETTINGS_STR, mJsonArray.toString());
        setResult(RESULT_OK, intent);
        finish();
    }
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.button){
                saveAndFinish();
            } else if (id == R.id.button2) {
                finish();
            }
        }
    };
    private BaseAdapter baseAdapter = new BaseAdapter() {
        public int getViewTypeCount() {
            return 4;
        }
        public int getItemViewType(int position) {
            JSONObject jsonObject = (JSONObject) getItem(position);
            String type = Objects.requireNonNull(jsonObject).optString(TemplateVunitTestActivity.SETTINGS_KEY_TYPE);
            switch (type) {
                case TemplateVunitTestActivity.SETTINGS_KEY_TYPE_VAL_ADDR:
                    return 0;
                case TemplateVunitTestActivity.SETTINGS_KEY_TYPE_VAL_STR:
                    return 1;
                case TemplateVunitTestActivity.SETTINGS_KEY_TYPE_VAL_BOX:
                    return 2;
                default:
                    return 3;
            }
        }
        public int getCount() {
            if (null != mJsonArray) {
                return mJsonArray.length();
            }
            return 0;
        }
        public Object getItem(int position) {
            try {
                return mJsonArray.getJSONObject(position);
            } catch (Exception e) {
                Log.e(TAG, "get json arr err", e);
            }
            return null;
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject jsonObject = (JSONObject) getItem(position);
            String val = Objects.requireNonNull(jsonObject).optString(TemplateVunitTestActivity.SETTINGS_KEY_VALUE);
            String desc = jsonObject.optString(TemplateVunitTestActivity.SETTINGS_KEY_DESCRIPTION);
            String key = jsonObject.optString(TemplateVunitTestActivity.SETTINGS_KEY_KEY);
            int type = getItemViewType(position);
            View v = convertView;
            if (null == v) {
                Log.i(TAG, "type=" + type);
                switch (type) {
                    case 0:
                        v = mLayoutInflater.inflate(R.layout.vunit_test_setting_item_button, null);
                        break;
                    case 1:
                        v = mLayoutInflater.inflate(R.layout.vunit_test_setting_item, null);
                        break;
                    case 2:
                        v = mLayoutInflater.inflate(R.layout.vunit_test_setting_item_check_box, null);
                        break;
                    default:
                        Log.e(TAG, "unhandle type=" + type);
                        break;
                }
            }
            if (null != v && 2 != type) {
                TextView textView = v.findViewById(R.id.textView);
                textView.setText(desc);
                EditText editText = v.findViewById(R.id.editText);
                editText.setText(val);
                editText.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    public void afterTextChanged(Editable s) {
                        Utils.putJsonObject(Utils.getJsonObjectFromJsonArray(mJsonArray,
                                position), TemplateVunitTestActivity.SETTINGS_KEY_VALUE,
                                editText.getText());
                    }
                });
                if (type == 0) {
                    Button button = v.findViewById(R.id.button);
                    button.setText("选择设备");
                    button.setOnClickListener(mOnClickListener);
                    button.setTag(position);
                }
            }
            else if(null != v) { // checkBox
                TextView textView = v.findViewById(R.id.textView);
                textView.setText(desc);
                CheckBox checkBox = v.findViewById(R.id.check_box);
                if(checkBox != null) {
                    checkBox.setChecked(val.equals("true"));
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Utils.putJsonObject(Utils.getJsonObjectFromJsonArray(mJsonArray,
                                            position), TemplateVunitTestActivity.SETTINGS_KEY_VALUE,
                                    isChecked);
                        }
                    });
                } else {
                    Log.e(TAG, "checkBox is null");
                }
            }
            return v;
        }
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                launchBtPicker((Integer) v.getTag());
            }
        };
    };
    private void launchBtPicker(int reqCode) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == bluetoothAdapter) return;
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        Intent searchIntent = new Intent(this, DeviceListActivity.class);
        searchIntent.putExtra(Utils.EXTRA_LAUNCH_BLE_DEVICE, false);
        startActivityForResult(searchIntent, reqCode);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult reqCode=" + requestCode + ", resCode=" + resultCode);
        switch (resultCode) {
            case Activity.RESULT_OK:
                BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) return;
                String dev = device.getAddress();
                Log.e(TAG,"VunitTestSettingsActivity.java:222 tangxinlou debug 3" +  new Object(){}.getClass().getEnclosingMethod().getName() + dev);
                Utils.putJsonObject(Utils.getJsonObjectFromJsonArray(mJsonArray,
                        requestCode), TemplateVunitTestActivity.SETTINGS_KEY_VALUE,
                        dev);
                baseAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
