package com.hust.indoorlocation.ui.main.survey;

import android.bluetooth.le.ScanResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hust.indoorlocation.R;
import com.hust.indoorlocation.base.BaseSlideAdapter;

import java.util.List;

public class BleInfoAdapter extends BaseSlideAdapter<ScanResult,BaseViewHolder>{

    public BleInfoAdapter(int layoutResId, @Nullable List<ScanResult> data) {
        super(layoutResId, data);
    }

    public BleInfoAdapter(@Nullable List<ScanResult> data) {
        super(R.layout.item_ble_info, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder viewHolder, ScanResult result) {
        viewHolder.setText(R.id.ble_device,"device: "+result.getDevice());
        viewHolder.setText(R.id.ble_DeviceName,"mDeviceName: "+result.getScanRecord().getDeviceName());
        viewHolder.setText(R.id.ble_rssi,"rssi: "+result.getRssi());
        viewHolder.setText(R.id.ble_timestampNanos,"timestampNanos: "+result.getTimestampNanos());
        viewHolder.setText(R.id.ble_describeContents,"describeContents: "+result.describeContents());
//        LogUtil.d("closeBt: 关闭蓝牙");
    }

}
