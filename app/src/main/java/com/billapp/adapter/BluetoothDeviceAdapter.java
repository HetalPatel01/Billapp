package com.billapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.billapp.R;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    private List<BluetoothConnection> devices;
    private OnDeviceClickListener listener;
    private Context context;
    private boolean showPairedDevices;

    public BluetoothDeviceAdapter(List<BluetoothConnection> devices, OnDeviceClickListener listener, Context context, boolean showPairedDevices) {
        this.devices = devices;
        this.listener = listener;
        this.context = context;
        this.showPairedDevices = showPairedDevices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothConnection device = devices.get(position);
        holder.deviceName.setText(device.getDevice().getName());
        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
        }
    }

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothConnection device);
    }
}
