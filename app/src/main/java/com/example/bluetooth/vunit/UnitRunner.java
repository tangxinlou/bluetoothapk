package com.example.bluetooth.vunit;
import android.content.Context;
import android.util.Log;
import com.vivo.android.bluetooth.toolkit.Collector;
import com.example.bluetooth.vunit.cases.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
public class UnitRunner extends UnitCase {
    private static final String TAG = "UnitRunner";
    public static final String UID = "UnitRunner";
    public static final String ID = "0.0.0.1";
    public static final String CONF_KEY_VAL = "v";
    public static final String CONF_KEY_TEST_TIMES = "t";
    public static final String CONF_KEY_TEST_INTERVAL = "i";
    public static final String CONF_KEY_TEST_TYPE = "tt";
    public static final String CONF_KEY_SETTINGS = "s";
    public static final String CONF_KEY_COLLECTOR_FLAG = "cf";
    public static final String CONF_KEY_NEED_INDEX = "ni";
    private String mCollectorResult = "";
    protected List<UnitCase> mUnitCases = new ArrayList<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Future<String> future = null;
    public static final int DEFAULT_TEST_TIMES = 1;
    public static final int DEFAULT_TEST_INTERVAL = 0;
    private int mTestTimes = DEFAULT_TEST_TIMES;
    private int mTestInterval = DEFAULT_TEST_INTERVAL;// ms
    private String mTestType = null;
    private int mCurIndex = 0;
    private String mID = "0001";
    public UnitRunner(Context context) {
        super(context);
    }
    protected void running()  {
        Log.i(TAG, "running()");
        if (mUnitCaseState == STATE_SKIP) return;
        if (mHighestMsgLevel == Log.ASSERT) return;
        for (mTestIndex = 0; mTestIndex < mTestTimes; mTestIndex++) {
            mCurIndex = mTestIndex;
            int level = Log.VERBOSE;
            if (mTestIndex > 0) {
                try {
                    Thread.sleep(mTestInterval);
                } catch (InterruptedException e) {
                    Log.w(TAG, "interupt by=" + e);
                }
            }
            for (UnitCase unitCase : mUnitCases) { // 小循环
                if (mUnitCaseState == STATE_SKIP) return;
                Log.v(TAG, "running case=" + unitCase.getTag());
                if (unitCase.getWatchDogTime() == 0) {
                    Log.w(TAG, "cur case test Num 0, skip: " + unitCase.getTag());
                    continue;
                }
                UnitCaseManager.getInstance().addCase(unitCase.getID(), unitCase.getTag());
                UnitCaseManager.getInstance().addCase(unitCase.getID(), unitCase.getTag());
                unitCase.registStateCb(mStateCb, unitCase.getTag());
                future = executorService.submit(() -> {unitCase.run();return null;});
                try {
                    future.get(unitCase.getWatchDogTime(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    onCaseResult(unitCase.getWatchDogLevel(),
                            "case timeout, case=" + unitCase.getTag());
                    Log.e(TAG, "TimeoutException: " + e);
                    unitCase.mHighestMsgLevel = Log.ERROR;
                    future.cancel(true);
                } catch (InterruptedException | ExecutionException e) {
                    onCaseResult(Log.ERROR,
                            "runtime err, case=" + unitCase.getTag());
                    unitCase.mHighestMsgLevel = Log.ERROR;
                    future.cancel(true);
                    Log.e(TAG, "InterruptedException | ExecutionException: " + e, e);
                }
                unitCase.unregistStateCb(mStateCb);
                if (mHighestMsgLevel == Log.ASSERT) break;
                if (level < unitCase.getHighestMsgLevel()) level =
                        unitCase.getHighestMsgLevel();
            }
            if (mHighestMsgLevel == Log.ASSERT) break;
            if (mTestTimes > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("第");
                sb.append(mTestIndex + 1);
                sb.append("次");
                if (mTestType != null) {
                    sb.append(mTestType);
                } else {
                    sb.append("测试");
                }
                if(level >= Log.WARN) {
                    sb.append("完成,请向前查看报错内容");
                } else {
                    sb.append("完成");
                }
                UnitRunner.this.onCaseResult(level, sb.toString());
            } else {
                StringBuilder sb = new StringBuilder();
                if (mTestType != null) {
                    sb.append(mTestType);
                } else {
                    sb.append("测试");
                }
                if(level >= Log.WARN) {
                    sb.append("完成,请向前查看报错内容");
                } else {
                    sb.append("完成");
                }
                UnitRunner.this.onCaseResult(level, sb.toString());
            }
        }
    }
    public void beforeRun() {
        super.beforeRun();
        for (UnitCase unitCase : mUnitCases) {
            if (mUnitCaseState == STATE_SKIP) break;
            if (mHighestMsgLevel == Log.ASSERT) break;
            unitCase.registStateCb(mStateCb, unitCase.getTag());
            unitCase.beforeRun();
            unitCase.unregistStateCb(mStateCb);
        }
    }
    public void afterRun() {
        super.afterRun();
        for (UnitCase unitCase : mUnitCases) { // 小循环
            if (mUnitCaseState == STATE_SKIP) break;
            if (mHighestMsgLevel == Log.ASSERT) break;
            unitCase.registStateCb(mStateCb, unitCase.getTag());
            unitCase.afterRun();
            unitCase.unregistStateCb(mStateCb);
        }
    }
    public void stop() {
        super.stop();
        for (UnitCase unitCase : mUnitCases) {
            unitCase.stop();
        }
        if (null != future && !future.isDone()) {
            future.cancel(true);
            future = null;
        }
        mCollectorResult = "";
    }
    public int size() {
        int len = 0;
        for (UnitCase unitCase : mUnitCases) {
            len += unitCase.size();
        }
        return len;
    }
    public long getWatchDogTime() {
        long watchDogTime = 0;
        for (UnitCase unitCase : mUnitCases) {
            watchDogTime += unitCase.getWatchDogTime();
        }
        return (watchDogTime + mTestInterval) * mTestTimes ;  // 需要计算 unitRunner sleep 时间
    };
    public int getWatchDogLevel() {
        int level = Log.VERBOSE;
        for (UnitCase unitCase : mUnitCases) {
            if (level < unitCase.getWatchDogLevel()) level = unitCase.getWatchDogLevel();
        }
        return level;
    };
    StateCb mStateCb = new StateCb() {
        public void onStateChanged(int prev, int toState) {
        }
        public void onCaseResult(int index, UnitResult unitResult) {
            UnitRunner.this.onCaseResult(unitResult.mLevel, unitResult.mMsg);
        }
    };
    public void config(JSONObject jsonObject) {
        if (null == jsonObject) {
            Log.w(TAG, "empty config");
            return;
        }
        if (!(UID.equals(uid) || MultiThreadUnitRunner.UID.equals(uid))) {
            Log.wtf(TAG, "unhandle jsonobject=" + jsonObject);
        }
        String uid = jsonObject.optString(UnitCase.CONF_KEY_UIDS);
        if (!(UID.equals(uid) || MultiThreadUnitRunner.UID.equals(uid))) {
            Log.wtf(TAG, "unhandle jsonobject=" + jsonObject);
        }
        mTestTimes = Integer.valueOf(jsonObject.optString(CONF_KEY_TEST_TIMES,
                "" + DEFAULT_TEST_TIMES));
        mTestInterval = Integer.valueOf(jsonObject.optString(CONF_KEY_TEST_INTERVAL,
                "" + DEFAULT_TEST_INTERVAL));
        mTestType = jsonObject.optString(CONF_KEY_TEST_TYPE, null);
        JSONArray jsonArray = jsonObject.optJSONArray(CONF_KEY_VAL);
        mUnitCases.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject1 = jsonArray.optJSONObject(i);
            UnitCase unitCase =
                    createUnitCaseByUids(jsonObject1.optString(UnitCase.CONF_KEY_UIDS), mContext);
            unitCase.config(jsonObject1);
            mUnitCases.add(unitCase);
        }
    };
    public String getTag() {
        return TAG;
    }
    public String getID() {
        return ID;
    }
    public String getSummary() {
        return mCollectorResult;
    }
            case CheckConnectedCase.UID: return new CheckConnectedCase(context);
            case StandbyConnectionCheck.UID: return new StandbyConnectionCheck(context);
    private static UnitCase createUnitCaseByUids(String uids, Context context) {
        switch (uids){
            case TurnOnBT.UID:return new TurnOnBT(context);
            case TurnOffBT.UID:return new TurnOffBT(context);
            case CheckConnectedCase.UID: return new CheckConnectedCase(context);
            case StandbyConnectionCheck.UID: return new StandbyConnectionCheck(context);
        }
        return null;
    }
}
