package com.example.bluetooth.vunit.cases;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.example.bluetooth.BluetoothStateController;
import com.example.bluetooth.vunit.UnitCase;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
public class TurnOffBT extends UnitCase {
    private static final String TAG = "TurnOffBT";
    public static final String UID = "TurnOffBT";
    public static final String ID = "1.1.1.2";
    private Semaphore semaphore = new Semaphore(0);
    private static final String ACTION_BLE_STATE_CHANGED = "android.bluetooth.adapter.action" +".BLE_STATE_CHANGED";
    public static final String CONF_FORCE_NOT_OFF_STATE = "notOff";
    private boolean mForceNotOff = false;
    public TurnOffBT(Context context) {
        super(context);
    }
    protected void running()  {
        int releaseSem = semaphore.drainPermits();
        Log.v(TAG, "cur sema num: " + semaphore.availablePermits() + "， releaseSem num: " + releaseSem);
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_BLE_STATE_CHANGED);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        notEquals(adapter, null, Log.ASSERT, "no adapter!");
        if (Log.ASSERT == mHighestMsgLevel) return;
        if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
            if (mForceNotOff) {
                notEquals(BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_OFF, Log.ASSERT, "already off");
            } else {
                return;
            }
        }
        if (Log.ASSERT == mHighestMsgLevel) return;
        mContext.registerReceiver(broadcastReceiver, intentFilter);
        BluetoothStateController.getInstance().regCb(mBluetoothStateControllerCallback,
                BluetoothStateController.REGISTER_TYPE_WORKER);
        adapter.disable();
        try {
            Log.v(TAG, "req sema");
            semaphore.acquire();
        } catch (InterruptedException e) {
            Log.e(TAG, "sema fail", e);
        }
        mContext.unregisterReceiver(broadcastReceiver);
        BluetoothStateController.getInstance().unregCb(mBluetoothStateControllerCallback);
        try {
            Thread.sleep(200);
        } catch(Exception e) {
            Log.w(TAG, "err=" + e, e);
        }
    }
    public String getTag() {
        return TAG;
    }
    public String getID() {
        return ID;
    }
    public void config(JSONObject jsonObject) {
        super.config(jsonObject);
        mForceNotOff = 0 != jsonObject.optInt(CONF_FORCE_NOT_OFF_STATE, 1);
    };
    BluetoothStateController.Callback mBluetoothStateControllerCallback = new BluetoothStateController.Callback() {
        public void stateChange(int prevState, int nextState) {
        }
        public void errorStateChange(int prevState, int nextState) {
        }
    };
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                if (BluetoothAdapter.STATE_OFF == intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_ON)) {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    boolean isLePresent = false;
                    try {
                        Method m = BluetoothAdapter.class.getMethod("getBluetoothManager");
                        Object iBluetoothManager = m.invoke(bluetoothAdapter);
                        Class<?> clazz = Class.forName("android.bluetooth.IBluetoothManager");
                        m = clazz.getMethod("isBleAppPresent");
                        isLePresent = (boolean)m.invoke(iBluetoothManager);
                        Log.v(TAG, "isLePresent=" + isLePresent);
                    } catch (Exception e) {
                        Log.e(TAG, "err=" + e, e);
                    }
                    if (isLePresent) {
                        Log.v(TAG, "release sema");
                        semaphore.release();
                    }
                }
            } else if (ACTION_BLE_STATE_CHANGED.equals(intent.getAction())) {
                if (BluetoothAdapter.STATE_OFF == intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_ON)) {
                    Log.v(TAG, "release sema by le off");
                    semaphore.release();
                }
            }
        }
    };
}
