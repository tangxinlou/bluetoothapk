package com.example.bluetooth.interconnect;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.bluetooth.R;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    
    private List<BluetoothDevice> deviceList;
    private OnDeviceClickListener onDeviceClickListener;
    
    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }
    
    public DeviceAdapter(List<BluetoothDevice> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.onDeviceClickListener = listener;
    }
    
    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.bind(device, onDeviceClickListener);
    }
    
    @Override
    public int getItemCount() {
        return deviceList.size();
    }
    
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceName;
        private TextView deviceAddress;
        
        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceAddress = itemView.findViewById(R.id.deviceAddress);
        }

        public void bind(BluetoothDevice device, OnDeviceClickListener listener) {
            deviceName.setText(device.getName() != null ? device.getName() : "未知设备");
            deviceAddress.setText(device.getAddress());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }
    }
}
