package com.example.bluetooth.vunit;

import android.util.Log;

public class UnitResult {
    public int mLevel = Log.VERBOSE;
    public String mMsg = "";
    public String mID = "";
    public UnitResult(String msg) {
        mMsg = msg;
    }
    public UnitResult(int level, String msg) {
        mLevel = level;
        mMsg = msg;
    }
    public UnitResult(int level, String msg, String id) {
        mLevel = level;
        mMsg = msg;
        mID = id;
    }
}
