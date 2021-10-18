package com.hust.indoorlocation.ui.main.survey;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hust.indoorlocation.R;

import com.hust.indoorlocation.base.BaseListActivity;
import com.hust.indoorlocation.databinding.ActivitySurveyBleBinding;
import com.hust.indoorlocation.tools.util.LogUtil;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleSurveyActivity extends BaseListActivity<ActivitySurveyBleBinding,ScanResult>{

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private ScanSettings scanSettings;
    private BluetoothLeScanner mScanner;
    private static final String title = "ble";

    private TextView bleText;

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarBase);
        toolbar.setTitle(this.getClass().getSimpleName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public final void startBt() {
        //初始化ble设配器
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //4.初始化蓝牙适配器
        if (bluetoothAdapter == null) {
            Toast.makeText(this, (CharSequence) "当前设备不支持蓝牙 Device doesn't support Bluetooth\n", Toast.LENGTH_SHORT).show();
            return;
        }

        //5.当前设备是否已开启蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //设置过滤条件
        scanSettings = new ScanSettings.Builder()
                //设置高功耗模式
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
                .setReportDelay(0L)
                .build();
        // bluetoothAdapter.startLeScan(callback);
        LogUtil.d("startBt: 开启蓝牙");
    }

    public final void closeBt() {
        //关闭蓝牙
        if (bluetoothAdapter.isEnabled()) {
            //   bluetoothAdapter.disable();
        }
        stopScan();
        // LogUtil.d("closeBt: 关闭蓝牙");
    }

    public final void startScan() {
        //开始扫描
        mScanner = bluetoothAdapter.getBluetoothLeScanner();
        mScanner.startScan(null, scanSettings, scanCallback);
    }

    public final void stopScan() {
        mScanner.stopScan(scanCallback);
    }

    private List<ScanResult> mIbenconList = new ArrayList<>();
    private Map<String, Integer> mIbenconMap = new HashMap<>();
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
//            String bleInfo="device: "+result.getDevice()+"\n"
//                    +"mDeviceName: "+result.getScanRecord().getDeviceName()+"\n"
//                    +"rssi: "+result.getRssi()+"\n"
//                    +"timestampNanos: "+result.getTimestampNanos()+"\n"
//                    +"describeContents: "+result.describeContents()+"\n" ;
            //  LogUtil.d("closeBt: 关闭蓝牙"+bleInfo);
            if (result != null && result.getScanRecord() != null) {
                List<ADStructure> structures = ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
                for (ADStructure structure : structures) {
                    if (structure instanceof IBeacon) {
                        String bleInfoHead="uuid=" + ((IBeacon) structure).getUUID() + "\n"+
                                "major="+ ((IBeacon) structure).getMajor() +"\n"+
                                "minor=" + ((IBeacon) structure).getMinor() +"\n"+
                                "power=" + ((IBeacon) structure).getPower()+"\n";
                   //     LogUtil.d(bleInfoHead);
                        String bleInfo = bleInfoHead+ "\n"
                                + "device: " + result.getDevice() + "\n"
                                + "mDeviceName: " + result.getScanRecord().getDeviceName() + "\n"
                                + "rssi: " + result.getRssi() + "\n"
                                + "timestampNanos: " + result.getTimestampNanos() + "\n"
                                + "describeContents: " + result.describeContents() + "\n";
                        bleText.setText(bleInfo);
                        if(!mIbenconMap.containsKey(result.getDevice().toString())){
                            mIbenconList.add(result);
                            getMListAdapter().addData(result);
                           // showAtAdapter(mIbenconList);
                        }else{
                            mIbenconMap.put(result.getDevice().toString(),mIbenconMap.get(result.getDevice())+1);
                        }
                    }
                }
            }
        }

        //批量返回扫描结果
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        //当扫描不能开启时回调
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    // 申请打开蓝牙请求的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "没有蓝牙权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isSurvey)
            closeBt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // startBt();
    }

    @Override
    public void initListView() {
        initActionBar();
        initBottomNav();
        startBt();
        bleText = (TextView) findViewById(R.id.bleText);
        bleText.setText("收集蓝牙数据前请同时打开位置信息和蓝牙");
        setMRecyclerView(mBinding.recyclerView) ;
        setMSwipeRefreshLayout(mBinding.swipeRefreshLayout);
    }

    boolean isSurvey=false;
    /**
     * bottom_navigation 点击事件
     */
    private void initBottomNav() {
        mBinding.bottomNavigationBle.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_ble_scan:
                        if(!isSurvey){
                            isSurvey=true;
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_pause_24);
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setTitle("停止");
                            startScan();
                        }else{
                            isSurvey=false;
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_play_arrow_24);
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setTitle("开始");
                            stopScan();
                        }
                        break;
                    case R.id.action_ble_help:

                        break;

                    default:
                        break;
                }
                return false;
            }
        });
    }
    @NonNull
    @Override
    public ActivitySurveyBleBinding attachViewBinding() {
        return ActivitySurveyBleBinding.inflate(getLayoutInflater());
    }


    @NonNull
    @Override
    public BaseQuickAdapter<ScanResult, ?> attachAdapter() {
        return new BleInfoAdapter(null);
    }
}