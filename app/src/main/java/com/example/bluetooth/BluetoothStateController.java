package com.example.bluetooth;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
public class BluetoothStateController {
    private static final String TAG = "BluetoothStateController";
    private static BluetoothStateController instance;
    private static final Object instanceLock = new Object();
    public static BluetoothStateController getInstance() {
        if (null == instance) {
            synchronized (instanceLock) {
                if (null == instance) {
                    instance = new BluetoothStateController();
                }
            }
        }
        return instance;
    }
    public static final int REGISTER_TYPE_LISTENER = 0;
    public static final int REGISTER_TYPE_WORKER = 10;
    private static final String ACTION_BLE_STATE_CHANGED = "android.bluetooth.adapter.action" +".BLE_STATE_CHANGED";
    private static final int STATE_BLE_TURNING_ON = 14;
    private static final int STATE_BLE_ON = 15;
    private static final int STATE_BLE_TURNING_OFF = 16;
    private Context mContext = null;
    private int mState = BluetoothAdapter.STATE_OFF;
    private Map<Integer, Set<Callback>> mCbs = new TreeMap<>();
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothStateController() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = getState();
        mCbs.put(REGISTER_TYPE_LISTENER, new HashSet());
        mCbs.put(REGISTER_TYPE_WORKER, new HashSet());
    }
    public void init(Context context) {
        Log.i(TAG, "init()");
        if (mContext == null) {
            mContext = context;
            IntentFilter intentFilter = new IntentFilter(ACTION_BLE_STATE_CHANGED);
            mContext.registerReceiver(mBroadcastReceiver, intentFilter);
        }
    }
    public void cleanup() {
        if (null != mContext) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mContext = null;
        }
    }
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_BLE_STATE_CHANGED: {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.STATE_OFF);
                    Log.d(TAG, "stateChange() " + mState + "->" + state);
                    List<Callback> list = new ArrayList<>();
                    synchronized (mCbs) {
                        for (Collection<Callback> c : mCbs.values()) {
                            for (Callback cb : c) {
                                list.add(cb);
                            }
                        }
                    }
                    for (Callback cb : list) {
                        cb.stateChange(mState, state);
                    }
                    if (!checkLegalStateChange(mState, state)) {
                        List<Callback> list2 = new ArrayList<>();
                        Log.d(TAG, "errorStateChange() " + mState + "->" + state);
                        synchronized (mCbs) {
                            if (mCbs.get(REGISTER_TYPE_WORKER).size() > 0) {
                                for (Callback cb : mCbs.get(REGISTER_TYPE_WORKER)) {
                                    list2.add(cb);
                                }
                            } else {
                                for (Callback cb : mCbs.get(REGISTER_TYPE_LISTENER)) {
                                    list2.add(cb);
                                }
                            }
                            for (Callback cb : list2) {
                                cb.errorStateChange(mState, state);
                            }
                        }
                    }
                    mState = state;
                    break;
                }
            }
        }
    };
    public static class Callback {
        public void stateChange(int prevState, int nextState) {}
        public void errorStateChange(int prevState, int nextState) {}
    }
    public void regCb(Callback cb) {
        regCb(cb, REGISTER_TYPE_LISTENER);
    }
    public void regCb(Callback cb, int type) {
        synchronized (mCbs) {
            if (mCbs.containsKey(type)) {
                mCbs.get(type).add(cb);
            }
        }
    }
    public void unregCb(Callback cb) {
        synchronized (mCbs) {
            for (Set set : mCbs.values()) {
                set.remove(cb);
            }
        }
    }
    private boolean checkLegalStateChange(int prevState, int nextState) {
        switch (prevState) {
            case BluetoothAdapter.STATE_OFF:{
                if (nextState == STATE_BLE_TURNING_ON
                        || nextState == BluetoothAdapter.STATE_TURNING_OFF)
                    return true;
                return false;
            }
            case STATE_BLE_TURNING_ON:{
                if (nextState == STATE_BLE_ON) return true;
                return false;
            }
            case STATE_BLE_ON:{
                if (nextState == BluetoothAdapter.STATE_TURNING_ON) return true;
                if (nextState == STATE_BLE_TURNING_OFF) return true;
                return false;
            }
            case BluetoothAdapter.STATE_TURNING_ON:{
                if (nextState == BluetoothAdapter.STATE_ON) return true;
                return false;
            }
            case BluetoothAdapter.STATE_ON:{
                if (nextState == BluetoothAdapter.STATE_TURNING_OFF) return true;
                return false;
            }
            case BluetoothAdapter.STATE_TURNING_OFF:{
                if (nextState == STATE_BLE_ON) return true;
                return false;
            }
            case STATE_BLE_TURNING_OFF:{
                if (nextState == BluetoothAdapter.STATE_OFF) return true;
                return false;
            }
            default:
                return false;
        }
    }
    public int getState() {
        return mBluetoothAdapter.getState();
    }
}
