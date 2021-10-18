package com.hust.indoorlocation.ui.main.collector;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;


import androidx.preference.PreferenceManager;

import com.hust.indoorlocation.ui.main.collector.consumer.WriteToFileConsumer;
import com.hust.indoorlocation.ui.main.collector.model.BleEvent;
import com.hust.indoorlocation.ui.main.collector.model.DataEvent;
import com.hust.indoorlocation.tools.util.LogUtil;
import com.hust.indoorlocation.tools.util.SensorUtil;
import com.hust.indoorlocation.tools.util.TimeUtil;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import kotlin.Pair;

/**
 * @author admin
 */
public class CollectorService extends Service
implements SensorEventListener {

    private final String TAG = "SensorsCollectorService";
    private SharedPreferences prefs;
    private SensorManager sensorManager;
    private final ArrayBlockingQueue sensorEventQueue = new ArrayBlockingQueue(256);
    private Thread consumer;
    private List<Sensor> supportedSensors;
    private HashMap<Integer,Integer> sensorsReceiveCount=new HashMap<>();
    private String carryType;
    private String environment;
    private String speedLevel;
    private Integer sensorDelay=0;
    private File dir;
    private Long startTimeMills;
    private int totalSensorEventCount=0;
    private int totalBeaconEventCount=0;
    private int totalBleEventCount=0;
    private int discardedEventCount=0;


    public CollectorService() {
    }

    public class DownloadBinder extends Binder{
        /**
         * 获取当前Service的实例
         * @return
         */
        public CollectorService getService(){
            return CollectorService.this;
        }
    }

    public void startDownload(){

    }

    public HashMap<Integer,Integer> getProgress(){
       // LogUtil.d("getProgress");
        return sensorsReceiveCount;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取系统传感器管理器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //每次启动service时调用
        getPreferences();
        supportedSensors=getSupportedSensors();


        LogUtil.d("service start:");
        LogUtil.d("context settings: carryType="+carryType +"environment="+environment+" speedLevel="+speedLevel);
        LogUtil.d("sensor  settings: sensorDelay="+sensorDelay);


        Long currentTimeMillis = System.currentTimeMillis();
        Long elapsedRealTimeNanos = SystemClock.elapsedRealtimeNanos();
        startTimeMills = currentTimeMillis;
        dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), String.valueOf(currentTimeMillis));
        if(!dir.exists()) {
            dir.mkdirs();
        }
        WriteToFileConsumer.Companion.createMetadata(
                Arrays.asList(
                        new Pair("currentTimeMillis", currentTimeMillis.toString()),
                        new Pair("elapsedRealTimeNanos", elapsedRealTimeNanos.toString()),
                        new Pair("guid", getGuid()),
                        new Pair("carryType", carryType.toString()),
                        new Pair("environment", environment.toString()),
                        new Pair("speedLevel", speedLevel.toString()),
                        new Pair("sensorDelay", sensorDelay.toString())
                ),
                dir
        );
        WriteToFileConsumer.Companion.createFiles(
                //返回仅包含与给定[predicate]匹配的元素的列表。
                supportedSensors, true, dir);

        //注册传感器监听者
        for (Sensor s:supportedSensors) {
            sensorManager.registerListener(this, s, sensorDelay);
            sensorsReceiveCount.put(s.getType(),0);
        }

        //蓝牙扫描
        ScanSettings scanSettings = new ScanSettings.Builder()
                //设置高功耗模式
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
                .setReportDelay(0L)
                .build();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothLeScanner scanner= bluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(null,scanSettings,scanCallback);

        // launch consumer thread
        CyclicBarrier barrier = new CyclicBarrier(2);
        consumer = new Thread(new SensorCollectorConsumerThread(sensorEventQueue, barrier, dir));
        consumer.start();
        try {
            barrier.await();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.sendBroadcastToCollector(3, "start at: " + TimeUtil.INSTANCE.getTimeStringFromCurrentTimeMillis(currentTimeMillis));
        this.sendBroadcastToCollector(1, "");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        consumer.interrupt();

        File newDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                TimeUtil.INSTANCE.getTimeStringFromCurrentTimeMillis(startTimeMills)
        );
        dir.renameTo(newDir);

        LogUtil.d("service stop"+sensorsReceiveCount);
        this.sendBroadcastToCollector(2, "");
        this.sendBroadcastToCollector(3, "total sensor event: " + this.totalSensorEventCount);
        this.sendBroadcastToCollector(3, "total beacon event: " + this.totalBeaconEventCount);
        this.sendBroadcastToCollector(3, "discarded event: " + this.discardedEventCount);
        this.sendBroadcastToCollector(3, "finished at: " + TimeUtil.INSTANCE.getDateTime()+ "\n\nready for next collecting");
        LogUtil.d("ble event count: " + this.totalBleEventCount);
        LogUtil.d("service destroyed");
    }

    private void getPreferences() {
        carryType = prefs.getString("carry_type", "hand").toString();
        environment = prefs.getString("environment", "lab").toString();
        speedLevel = prefs.getString("speed_level", "fast").toString();
        sensorDelay = Integer.parseInt(prefs.getString("sensor_delay", "0"));

        LogUtil.d("got prefs");
    }

    private String getGuid() {
        String guid = prefs.getString("guid", "").toString();
        if(guid.isEmpty()) {
            guid = UUID.randomUUID().toString();
            prefs.edit().putString("guid", guid);
            prefs.edit().apply();
        }
        return guid;
    }

    private List getSupportedSensors() {
        List<Sensor> deviceSensors=sensorManager.getSensorList(Sensor.TYPE_ALL);
        ArrayList<Sensor> deviceSensors1 = new ArrayList<>();
        for (Sensor s:deviceSensors) {
            if(SensorUtil.INSTANCE.getSensor2misc().containsKey(s.getType())){
                deviceSensors1.add(s);
            }else{
               // deviceSensors.remove(s);
            }
        }
        LogUtil.d("got SupportedSensors");
        return deviceSensors1;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event == null) {
            return;
        }
        //LogUtil.d("event"+event);
        totalSensorEventCount++;
        if(!sensorEventQueue.offer(new DataEvent(DataEvent.Companion.getTYPE_SENSOR_EVENT(), event))) {
            discardedEventCount++;
            LogUtil.d("queue is full, event discarded");
        }
        if(sensorsReceiveCount.containsKey(event.sensor.getType())){
           // LogUtil.d("event"+event);
            int count=sensorsReceiveCount.get(event.sensor.getType());
            sensorsReceiveCount.put(event.sensor.getType(),++count);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }
    private ScanCallback scanCallback=new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            totalBleEventCount++;
            if(result != null && result.getScanRecord() != null) {
                List<ADStructure> structures= ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
                for (ADStructure structure: structures) {
                    if(structure instanceof IBeacon){
                        LogUtil.d("" + ((IBeacon)structure).getUUID() + ' ' + ((IBeacon)structure).getMajor() + ' ' + ((IBeacon)structure).getMinor() + ' ' + ((IBeacon)structure).getPower());
                        DataEvent event = new DataEvent(DataEvent.Companion.getTYPE_BLE_EVENT(), new BleEvent(result, ((IBeacon)structure).getMajor(), ((IBeacon)structure).getMinor()));
                        totalBeaconEventCount++;
                        if(!sensorEventQueue.offer(event)) {
                            discardedEventCount++;
                            LogUtil.d( "queue is full, event discarded");
                        }
                    }
                }
            }

          //  LogUtil.d("closeBt: 关闭蓝牙");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void sendBroadcastToCollector(int what, String info) {
        Intent intent = new Intent(this.getPackageName() + ".COLLECTOR_RECEIVER");
        intent.putExtra("what", what);
        intent.putExtra("info", info);
        this.sendBroadcast(intent);
    }


}