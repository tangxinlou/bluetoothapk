package com.example.bluetooth.vunit;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
public abstract class UnitCase {
    private static final String TAG = "UnitCase";
    private static final String ID = "0.0.0.0";
    public static final String CONF_KEY_UIDS = "u";
    public static final String UID = "unitCase";
    public static final String CONF_KEY_ADDR = "a";
    public static final String DEFAULT_ADDR = "00:00:00:00:00:00";
    public static final int STATE_STOPPED = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_SKIP = 2;
    public static final int STATE_BEFORE_RUN = 3;
    protected List<UnitResult> mUnitResults = new ArrayList<>();
    protected int mTestIndex = 0;
    protected int mUnitCaseState = STATE_STOPPED;
    protected int mHighestMsgLevel = Log.VERBOSE;
    protected Context mContext;
    private final Map<StateCb, Handler> mStateCbHandlerMap = new HashMap<>();
    private Map<StateCb, String> mStateCbStringMap = new HashMap<>();
    protected Semaphore mUnitCaseSemaphore = new Semaphore(0);
    public UnitCase(Context context) {
        mContext = context;
    }
    public void config(JSONObject jsonObject) {};
    public JSONObject readConfig() { return null; };
    public void run() {
        int prevState = mUnitCaseState;
        mUnitCaseState = STATE_RUNNING;
        onStateChanged(prevState, mUnitCaseState);
        running();
        prevState = mUnitCaseState;
        mUnitCaseState = STATE_STOPPED;
        onStateChanged(prevState, mUnitCaseState);
    }
    protected abstract void running();
    public void stop() {
        if (STATE_RUNNING == mUnitCaseState
            || STATE_BEFORE_RUN == mUnitCaseState) {
            mUnitCaseState = STATE_SKIP;
        }
    };
    public int size() { return 1; };
    public long getWatchDogTime() { return 5000; };
    public int getWatchDogLevel() { return Log.ERROR; };
    public interface StateCb {
        public void onStateChanged(int prev, int toState);
        public void onCaseResult(int index, UnitResult unitResult);
    }
    protected void equals(Object a, Object b, int level, String msg)  {
        if (STATE_RUNNING != mUnitCaseState && STATE_BEFORE_RUN != mUnitCaseState) return;
        if (a == b) return;
        if (null == a || null == b) {
            onCaseResult(level, msg);
            return;
        }
        if (a.getClass() == b.getClass()) {
            if (a.equals(b)) return;
        }
        onCaseResult(level, msg);
        return;
    }
    protected void notEquals(Object a, Object b, int level, String msg) {
        if (STATE_RUNNING != mUnitCaseState && STATE_BEFORE_RUN != mUnitCaseState) return;
        if (a == b) {
            onCaseResult(level, msg);
            return;
        }
        if (null == a || null == b) return;
        if (a.getClass() == b.getClass()) {
            if (a.equals(b)) {
                onCaseResult(level, msg);
                return;
            }
        }
    }
    protected void onCaseResult(int level, String msg) {
        if (level > mHighestMsgLevel) {
            mHighestMsgLevel = level;
        }
        synchronized(mStateCbHandlerMap) {
            for (StateCb stateCb : mStateCbHandlerMap.keySet()) {
                Handler handler = mStateCbHandlerMap.get(stateCb);
                if (null == handler) {
                    stateCb.onCaseResult(mTestIndex, new UnitResult(level, msg, getID()));
                } else {
                    int index = mTestIndex;
                    Log.d(getTag(), "index=" + index + ", level=" + level + ", msg=" + msg);
                    handler.post(() -> stateCb.onCaseResult(index, new UnitResult(level, msg, getID())));
                }
            }
        }
    }
    protected void onStateChanged(int prev, int toState) {
        synchronized(mStateCbHandlerMap) {
            for (StateCb stateCb : mStateCbHandlerMap.keySet()) {
                Handler handler = mStateCbHandlerMap.get(stateCb);
                if (null == handler) {
                    stateCb.onStateChanged(prev, toState);
                } else {
                    handler.post(() -> stateCb.onStateChanged(prev, toState));
                }
            }
        }
    }
    public StateCb getVunitTestStateCb() {
        for (Map.Entry<StateCb, String> entry : mStateCbStringMap.entrySet()) {
            if (entry.getValue().equals("TemplateVunitTestActivity")) {
                return entry.getKey();
            }
        }
        Log.e(TAG, "getVunitTestStateCb return null");
        return null;
    }
    public void registStateCb(StateCb stateCb, String type) {
        synchronized(mStateCbHandlerMap) {
            mStateCbHandlerMap.put(stateCb, null);
            mStateCbStringMap.put(stateCb, type);
        }
    }
    public void unregistStateCb(StateCb stateCb) {
        synchronized(mStateCbHandlerMap) {
            mStateCbHandlerMap.remove(stateCb);
            mStateCbStringMap.remove(stateCb);
        }
    }
    public List<UnitResult> getUnitResults() {
        return mUnitResults;
    }
    public int getHighestMsgLevel() {
        return mHighestMsgLevel;
    }
    public String getTag() {
        return TAG;
    }
    public String getID() {
        return ID;
    }
    public void beforeRun() {
        mUnitResults.clear();
        mTestIndex = 0;
        mHighestMsgLevel = Log.VERBOSE;
        int prevState = mUnitCaseState;
        mUnitCaseState = STATE_BEFORE_RUN;
        onStateChanged(prevState, mUnitCaseState);
    }
    public void afterRun() {}
    protected void drainPermitsSemaphore() {
        Log.v(getTag(), "drainPermitsSemaphore()");
        mUnitCaseSemaphore.drainPermits();
    }
    protected boolean acquireSemaphore() {
        try {
            Log.v(getTag(), "acquireSemaphore()");
            mUnitCaseSemaphore.acquire();
        } catch (InterruptedException e) {
            Log.e(getTag(), "sema fail", e);
            return false;
        }
        return true;
    }
    protected void releaseSemaphore() {
        Log.v(getTag(), "releaseSemaphore()");
        mUnitCaseSemaphore.release();
    }
}
