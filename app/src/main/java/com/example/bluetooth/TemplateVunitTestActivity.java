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
import com.example.bluetooth.vunit.UnitCase;
import com.example.bluetooth.vunit.UnitResult;
import com.example.bluetooth.vunit.UnitRunner;
import android.os.PowerManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;
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

    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_TESTING = 1;

    private PowerManager.WakeLock mWakeLock;

    Future<String> mFuture = null;
    ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    protected UnitRunner mUnitRunner;
    protected int mState;


    public TemplateVunitTestActivity() {
        mState = STATE_DEFAULT;
        ClassName = getClass().getSimpleName();
        mUnitRunner = new UnitRunner(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("基类"); 
        setContentView(R.layout.activity_templatevunittestactivity);
        mSettings = getSettingsDefaultJson();
        ListView tableListView = (ListView) findViewById(R.id.result_main_record_panel);
        mListViewAdapter = new TableAdapter(this, mList);
        tableListView.setAdapter(mListViewAdapter);
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
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
               switch (mState) {
                   case STATE_DEFAULT:
                       processStateBaseChange(STATE_TESTING);
                       break;
                   case STATE_TESTING:
                       processStateBaseChange(STATE_DEFAULT);
                       break;
               }
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


    protected void processStateBaseChange(int next) {
        int prev = mState;
        mState = next;
        Log.d(TAG, "processStateBaseChange() prev=" + prev + ", next=" + next);
        switch (prev) {
            case STATE_DEFAULT:{
                switch (next) {
                    case STATE_TESTING:
                        startTest();
                        break;
                    default:
                        mState = prev;
                        break;
                }
                break;
            }
            case STATE_TESTING:{
                switch (next) {
                    case STATE_DEFAULT:
                        //stopTest();
                        break;
                    default:
                        mState = prev;
                        break;
                }
                break;
            }
            default:
                mState = prev;
        }
    }



    protected void startTest() {
        Log.d(TAG,  "startTest()");
        mWakeLock.acquire();
        clearList();
        mTestCnt = 0;
        mTestCnt1 = 0;
        mStartButton.setText("停止");
        mUnitRunner.config(getConfig());
        mUnitRunner.registStateCb(mStateCb, TAG);
        mFuture = mExecutorService.submit(() -> {
            try {
                mUnitRunner.beforeRun();
                mUnitRunner.run();
                mUnitRunner.afterRun();
            } catch (Exception e) {
                Log.e(TAG, "Err=" + e, e);
            }
            return null;});
        mStartButton.setText("停止");
        updateInforText("正在进行第1轮测试");
    }


    private UnitCase.StateCb mStateCb = new UnitCase.StateCb() {
        public void onStateChanged(int prev, int toState) {
                TemplateVunitTestActivity.this.onStateChanged(prev, toState);
        }
        public void onCaseResult(int index, UnitResult unitResult) {
                TemplateVunitTestActivity.this.onCaseResult(index, unitResult);
        }
    };
    abstract protected JSONArray getSettingsDefaultJson();
    protected abstract JSONObject getConfig();
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
    protected void onStateChanged(int prev, int toState) {
        if (UnitCase.STATE_STOPPED == toState) {
            switch (mState) {
                case STATE_TESTING:
                    processStateBaseChange(STATE_DEFAULT);
                    break;
            }
        }
    }
    protected void onCaseResult(int index, UnitResult unitResult) {
        Log.i(TAG,"onCaseResult index=" + index + ", level=" + unitResult.mLevel + ", msg=" + unitResult.mMsg);
        if (unitResult.mLevel >= Log.ERROR) {
        }
        refreshTable(unitResult);
    }

    public static String getDateTime(String formatString, long timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        String dateTime = format.format(new Date(timeStamp));
        return dateTime;// 2012-10-03 23:41:31
    }

    public void refreshTable(UnitResult unitResult) {
        if (unitResult == null) return;
        BtResultData data = new BtResultData();
        data.setId(String.valueOf(100));
        data.setTimeStamp(getDateTime("HH:mm:ss", System.currentTimeMillis()));
        data.status = unitResult.mLevel;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(Utils.logLevelToString(unitResult.mLevel));
        sb.append("], ");
        sb.append(unitResult.mMsg);
        data.setResult(sb.toString());
        mList.add(data);
        mListViewAdapter.updateData(mList);
        mListViewAdapter.notifyDataSetChanged();
    }
}
