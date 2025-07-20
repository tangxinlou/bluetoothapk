package com.example.bluetooth.vunit.cases;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.example.bluetooth.BluetoothStateController;
import com.example.bluetooth.vunit.UnitCase;
import org.json.JSONObject;
import java.util.concurrent.Semaphore;
import java.lang.reflect.Method;
public class TurnOnBT extends UnitCase {
    private static final String TAG = "TurnOnBT";
    public static final String UID = "TurnOnBT";
    public static final String ID = "1.1.1.1";
    public static final String CONF_TOUT = "tout";
    public static final int CONF_TOUT_DEFAULT = 3000;
    private int mTout = CONF_TOUT_DEFAULT;
    public static final String CONF_STRICT_BT_NOT_ON = "strictBtNotOn";
    public static final int VAL_STRICT_BT_NOT_ON_DEFAULT = 1;
    private int mStrict = VAL_STRICT_BT_NOT_ON_DEFAULT;
    private Semaphore mSemaphore = new Semaphore(0);
    private long mBeginTime = 0;
    private long dTime = 0;
    public TurnOnBT(Context context) {
        super(context);
    }
    protected void running() {
        mSemaphore.drainPermits();
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        notEquals(adapter, null, Log.ASSERT, "no adapter!");
        if (Log.ASSERT == mHighestMsgLevel) return;
        int state = adapter.getState();
        if (state == BluetoothAdapter.STATE_ON) {
            if (mStrict != 0) {
                notEquals(adapter.getState(), BluetoothAdapter.STATE_ON, Log.ASSERT, "already on");
                if (Log.ASSERT == mHighestMsgLevel) return;
            } else {
                return;
            }
        }
        BluetoothStateController.getInstance().regCb(mBluetoothStateControllerCallback,BluetoothStateController.REGISTER_TYPE_WORKER);
        mBeginTime = System.currentTimeMillis();
        try {
            Method enableMethod = BluetoothAdapter.class.getMethod("enable");
            enableMethod.setAccessible(true);
            enableMethod.invoke(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Log.v(TAG, "req sema");
            mSemaphore.acquire();
        } catch (InterruptedException e) {
            Log.e(TAG, "sema fail", e);
            if (mTout != CONF_TOUT_DEFAULT) {
                onCaseResult(Log.ASSERT, "bt not on");
            }
        } finally {
        }
        BluetoothStateController.getInstance().unregCb(mBluetoothStateControllerCallback);
    }
    public String getTag() {
        return TAG;
    }
    public String getID() {
        return ID;
    }
    public long getWatchDogTime() { return mTout; };
    BluetoothStateController.Callback mBluetoothStateControllerCallback = new BluetoothStateController.Callback() {
        public void stateChange(int prevState, int nextState) {
            if (BluetoothAdapter.STATE_ON == nextState) {
                dTime = System.currentTimeMillis() - mBeginTime;
                mSemaphore.release();
                Log.v(TAG, "release sema, enable bluetooth cost: " + dTime);
                onCaseResult(Log.VERBOSE, "enable bluetooth: " + dTime + "ms");
            }
        }
        public void errorStateChange(int prevState, int nextState) {
        }
    };
    public void config(JSONObject jsonObject) {
        super.config(jsonObject);
        mStrict = jsonObject.optInt(CONF_STRICT_BT_NOT_ON, VAL_STRICT_BT_NOT_ON_DEFAULT);
        mTout = jsonObject.optInt(CONF_TOUT, CONF_TOUT_DEFAULT);
    }
}
