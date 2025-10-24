package com.example.bluetooth.interconnect;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.UUID;

/**
 * BLE广播器 - 修复版本
 * 负责管理BLE广播的启动、停止和状态监控
 */
public class Advertiser {
    private static final String TAG = "Advertiser";
    
    // BLE服务UUID
    private static final UUID UUID_STEPFUN_SERVICE = UUID.fromString("00001807-0000-1000-8000-00805f9b34fb");
    
    // 成员变量
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private boolean mAdvertising = false;
    private Context mContext;
    
    // 广告回调实例
    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            mAdvertising = true;
            Log.i(TAG, "BLE广播启动成功");
        }
        
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            mAdvertising = false;
            Log.e(TAG, "BLE广播启动失败，错误码: " + errorCode);
        }
    };
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public Advertiser(Context context) {
        mContext = context;
        initializeAdvertiser();
    }
    
    /**
     * 初始化BLE广播器
     */
    private void initializeAdvertiser() {
        // 检查Android版本兼容性
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "设备不支持低功耗蓝牙广告功能(需要Android 5.0+)");
        }
        
        // 获取蓝牙适配器
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // 检查蓝牙适配器状态
        if (bluetoothAdapter == null) {
            Log.e(TAG, "设备不支持蓝牙功能");
        }
        
        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "蓝牙未开启，请先启用蓝牙");
        }
        
        // 获取BLE广播器实例
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            Log.e(TAG, "无法获取BLE广播器实例，设备可能不支持BLE广播");
        }
        
        Log.d(TAG, "BLE广播器初始化完成");
    }
    
    /**
     * 检查设备是否支持BLE广告功能
     * @return true-支持，false-不支持
     */
    public boolean isAdvertisingSupported() {
        return mBluetoothLeAdvertiser != null;
    }
    
    /**
     * 启动BLE广播
     * @return true-启动成功，false-启动失败
     */
    public boolean startAdvertising() {
        // 检查BLE广播器是否可用
        if (!isAdvertisingSupported()) {
            Log.e(TAG, "设备不支持BLE广告功能");
            return false;
        }
        
        // 检查是否已经在广播
        if (mAdvertising) {
            Log.w(TAG, "BLE广播已在运行中");
            return true;
        }
        
        // 配置广告参数
        AdvertiseSettings settings = createAdvertiseSettings();
        AdvertiseData advertiseData = createAdvertiseData();
        
        // 启动广播
        try {
            mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, mAdvertiseCallback);
            Log.i(TAG, "BLE广播启动指令已发送");
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "权限不足，无法启动BLE广播: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "启动BLE广播时发生异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 停止BLE广播
     */
    public void stopAdvertising() {
        if (mAdvertising && mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertising = false;
            Log.i(TAG, "BLE广播已停止");
        }
    }
    
    /**
     * 创建广告设置
     * @return 配置好的AdvertiseSettings对象
     */
    private AdvertiseSettings createAdvertiseSettings() {
        return new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build();
    }
    
    /**
     * 创建广告数据
     * @return 配置好的AdvertiseData对象
     */
    private AdvertiseData createAdvertiseData() {
        return new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(new ParcelUuid(UUID_STEPFUN_SERVICE))
            .setIncludeTxPowerLevel(true)
            .build();
    }
    
    /**
     * 检查是否正在广播
     * @return true-正在广播，false-未广播
     */
    public boolean isAdvertising() {
        return mAdvertising;
    }
    
    /**
     * 获取当前广播状态描述
     * @return 状态描述字符串
     */
    public String getAdvertisingStatus() {
        if (!isAdvertisingSupported()) {
            return "设备不支持BLE广告";
        }
        return mAdvertising ? "正在广播" : "未广播";
    }
    
    /**
     * 清理资源
     */
    public void destroy() {
        if (mAdvertising) {
            stopAdvertising();
        }
        mBluetoothLeAdvertiser = null;
        mContext = null;
    }
}
