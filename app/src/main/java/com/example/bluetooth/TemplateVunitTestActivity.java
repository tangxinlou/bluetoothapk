package com.example.bluetooth;
import android.os.Bundle;
import android.app.Activity;
import android.view.View.OnClickListener; 
import android.widget.Button;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import com.example.bluetooth.VunitTestSettingsActivity;
//import com.vivo.android.bluetooth.vunit.UnitCase;
//import com.vivo.android.bluetooth.vunit.UnitResult;
//import com.vivo.android.bluetooth.vunit.UnitRunner;
abstract public class TemplateVunitTestActivity extends Activity {
    public static final String TAG = "TemplateVunitTestActivity";
    private final String ClassName;
    protected  List<BtResultData> mList = new ArrayList<BtResultData>();
    private Button mStartButton;
    private Button mSettingButton;
    private Button mButtonSummary;
    private Button mButtonCheck;
    protected TextView mInfoText;
    protected TableAdapter mListViewAdapter;
    protected int mTestCnt = 0;
    protected int mTestCnt1 = 0;
    public static final String EXTRA_SETTINGS_STR = "settings_str";
    public static final String SETTINGS_KEY_KEY = "k";
    public static final String SETTINGS_KEY_VALUE = "v";
    public static final String SETTINGS_KEY_DESCRIPTION = "d";
    public static final String SETTINGS_KEY_TYPE = "t";
    public static final String SETTINGS_KEY_TYPE_VAL_ADDR = "a";
    public static final String SETTINGS_KEY_TYPE_VAL_STR = "s";
    public static final String SETTINGS_KEY_TYPE_VAL_BOX = "b";
    protected JSONArray mSettings;
    public TemplateVunitTestActivity() {
        ClassName = getClass().getSimpleName();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("基类"); 
        setContentView(R.layout.activity_templatevunittestactivity);
        mSettings = getSettingsDefaultJson();
        ListView tableListView = (ListView) findViewById(R.id.result_main_record_panel);
        mListViewAdapter = new TableAdapter(this, mList);
        tableListView.setAdapter(mListViewAdapter);
        
        clearList();
        mStartButton = (Button)findViewById(R.id.btn_start_search);
        mSettingButton = (Button)findViewById(R.id.btn_setting);
        mButtonSummary = (Button)findViewById(R.id.btn_summary);
        mButtonCheck = (Button)findViewById(R.id.btn_check_before_test);
        mStartButton.setOnClickListener(onClickListener);
        mSettingButton.setOnClickListener(onClickListener);
        mButtonSummary.setOnClickListener(onClickListener);
        mButtonCheck.setOnClickListener(onClickListener);
        mInfoText = (TextView)findViewById(R.id.infor);
        updateInforText("第一次测试");
    }
    public void updateInforText(String text) {
        if (null != mInfoText) {
            mInfoText.setText(text);
        }
    }
    private void clearList() {
        mList.clear();
        mList.add(new BtResultData("INDEX", "STAMP", "RESULT","RESULT2","DURATION"));
        if (null != mListViewAdapter) {
            mListViewAdapter.notifyDataSetChanged();
        }
    }
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
           if(id == R.id.btn_check_before_test) {
           }else if (id == R.id.btn_start_search){
           }else if (id == R.id.btn_setting){
               Intent connectionIntent = new Intent(TemplateVunitTestActivity.this,
                       VunitTestSettingsActivity.class);
               connectionIntent.putExtra(EXTRA_SETTINGS_STR, mSettings.toString());
               startActivityForResult(connectionIntent, 0);
           }else if (id == R.id.btn_summary) {
           }
        }
    };
    
    protected static JSONArray getJsonArrayFromList(List<String[]> list) {
        final int posKey = 0;
        final int posDesc = 1;
        final int posVal = 2;
        final int posType = 3;
        JSONArray jsonArray = new JSONArray();
        for (String[] strings : list) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(SETTINGS_KEY_KEY, strings[posKey]);
                jsonObject.put(SETTINGS_KEY_DESCRIPTION, strings[posDesc]);
                jsonObject.put(SETTINGS_KEY_VALUE, strings[posVal]);
                jsonObject.put(SETTINGS_KEY_TYPE, strings[posType]);
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                Log.wtf(TAG, "illigal string[]", e);
            }
        }
        return jsonArray;
    }
    abstract protected JSONArray getSettingsDefaultJson();
    protected void onDestroy() {
        super.onDestroy();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult reqCode=" + requestCode + ", resCode=" + resultCode);
        switch (resultCode) {
            case Activity.RESULT_OK:
                JSONArray jsonArray = Utils.getJsonArrayFromString(data.getStringExtra(EXTRA_SETTINGS_STR));
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (Utils.getJsonObjectFromJsonArray(jsonArray, i).optString(SETTINGS_KEY_KEY).equals(Utils.getJsonObjectFromJsonArray(mSettings, i).optString(SETTINGS_KEY_KEY))) {
                        Utils.putJsonObject(Utils.getJsonObjectFromJsonArray(mSettings, i),SETTINGS_KEY_VALUE,Utils.getJsonObjectFromJsonArray(jsonArray, i).optString(SETTINGS_KEY_VALUE));
                        continue;
                    } else {
                        Log.e(TAG,"onActivityResult noequal");
                    }
                }
                Log.i(TAG, "onActivityResult written=" + mSettings);
                break;
            default:
                break;
        }
    }
}
