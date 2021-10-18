/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.hust.indoorlocation;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.VersionInfo;
import com.hust.baiduimap.BMapApiDemoMain;
import com.hust.indoorlocation.ui.main.collector.CollectorActivity;
import com.hust.indoorlocation.ui.main.survey.SurveyActivity;
import com.hust.indoorlocation.ui.main.test.TestActivity;

import java.util.ArrayList;


/**
 * @author admin
 */
public class MainActivity extends AppCompatActivity {
    private static final String LTAG = MainActivity.class.getSimpleName();


    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            TextView text = (TextView) findViewById(R.id.text_Info);
            text.setTextColor(Color.RED);
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                text.setText("key 验证出错! 错误码 :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        +  " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                text.setText("key 验证成功! 功能可以正常使用");
                text.setTextColor(Color.GREEN);
            } else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                text.setText("网络出错");
            }
        }
    }

    private SDKReceiver mReceiver;
    private boolean isPermissionRequested;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbarBase);
        toolbar.setTitle("Indoor Location");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView text = (TextView) findViewById(R.id.text_Info);
        text.setTextColor(Color.GREEN);
        text.setText("室内定位信息收集测试");
        setTitle(getTitle() + " v" + VersionInfo.getApiVersion());

        ListView mListView = (ListView) findViewById(R.id.listView);
        // 添加ListItem，设置事件响应
        mListView.setAdapter(new DemoListAdapter());
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                onListItemClick(index);
            }
        });
        // 申请动态权限
        requestPermission();


        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.BLUETOOTH_ADMIN,

                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                    Toast.makeText(this, "permission denied: $deniedList", Toast.LENGTH_SHORT).show();
                }
            }

            if (!permissionsList.isEmpty()) {
                String[] strings = new String[permissionsList.size()];
                requestPermissions(permissionsList.toArray(strings), 0);
            }
        }
    }

    void onListItemClick(int index) {
        Intent intent;
        intent = new Intent(this, DEMOS[index].demoClass);
        this.startActivity(intent);
    }

    private static final DemoInfo[] DEMOS = {
            new DemoInfo(R.drawable.map, R.string.div_title_survey, R.string.div_desc_survey, SurveyActivity.class),
            new DemoInfo(R.drawable.map, R.string.div_title_collection, R.string.div_desc_location, CollectorActivity.class),
            new DemoInfo(R.drawable.map, R.string.div_title_test, R.string.div_desc_test, TestActivity.class),
            new DemoInfo(R.drawable.map, R.string.div_title_baiduMap, R.string.div_desc_baiduMap, BMapApiDemoMain.class),};

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消监听 SDK 广播
        unregisterReceiver(mReceiver);
    }

    private class DemoListAdapter extends BaseAdapter {
        private DemoListAdapter() {
            super();
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = View.inflate(MainActivity.this, R.layout.demo_item, null);
            }
            ImageView imageView =(ImageView)convertView.findViewById(R.id.image);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView desc = (TextView) convertView.findViewById(R.id.desc);
            imageView.setBackgroundResource(DEMOS[index].image);
            title.setText(DEMOS[index].title);
            desc.setText(DEMOS[index].desc);

            return convertView;
        }

        @Override
        public int getCount() {
            return DEMOS.length;
        }

        @Override
        public Object getItem(int index) {
            return DEMOS[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }

    private static class DemoInfo {
        private final int image;
        private final int title;
        private final int desc;
        private final Class<? extends Activity> demoClass;

        private DemoInfo(int image,int title, int desc, Class<? extends Activity> demoClass) {
            this.image = image;
            this.title = title;
            this.desc = desc;
            this.demoClass = demoClass;
        }
    }
}