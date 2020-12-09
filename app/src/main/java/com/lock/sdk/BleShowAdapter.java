package com.lock.sdk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.base.BleStatus;

import java.util.ArrayList;

public class BleShowAdapter extends RecyclerView.Adapter<BleShowAdapter.ViewHolder> {

    ArrayList<ChangesDeviceEvent> dataSet;
    ClickCallback callback;

    public BleShowAdapter(ClickCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ble, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChangesDeviceEvent event = dataSet.get(position);
        holder.tvAddress.setText(event.mBleBase.getAddress());
        holder.tvName.setText(event.mBleBase.getName());
        holder.tvPassword.setText(event.mBleBase.getPassWord());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.bleClicked(event);
            }
        });
//        holder.tvRssi.setText(event.mBleBase.);
    }

    public void statusUpdate(BleStatus status){

    }

    public void setDataSet(ArrayList<ChangesDeviceEvent> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvAddress;
        TextView tvRssi;
        TextView tvPassword;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.address);
            tvName = itemView.findViewById(R.id.name);
            tvRssi = itemView.findViewById(R.id.rssi);
            tvPassword = itemView.findViewById(R.id.password);
        }
    }
}
