package com.hust.indoorlocation.ui.main.collector;

import android.hardware.Sensor;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hust.indoorlocation.R;
import com.hust.indoorlocation.base.BaseSlideAdapter;
import com.hust.indoorlocation.tools.util.SensorUtil;

import java.util.List;

/**
 * @author admin
 */
public class CollectorAdapter extends BaseSlideAdapter<Sensor, BaseViewHolder> {

    public CollectorAdapter() {
        super(R.layout.item_collection_progress, null);
    }

    public CollectorAdapter(int layoutResId, @Nullable List<Sensor> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Sensor s) {
        if(SensorUtil.INSTANCE.getInfoMap().containsKey(s.getType())){
            viewHolder.setText(R.id.collectText, SensorUtil.INSTANCE.getInfoMap().get(s.getType()).sensorTypeName);
        }else{
            viewHolder.setText(R.id.collectText,s.getName()+" 未知类型");
        }
        viewHolder.setText(R.id.collectCount,"0");

    }
}