package com.hust.indoorlocation.ui.main.test.simulation;

import android.app.Service;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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

import com.hust.indoorlocation.locationMethods.pdr.NaivePdrConsumer;
import com.hust.indoorlocation.locationMethods.pdr.PDRcallback;
import com.hust.indoorlocation.locationMethods.pdr.SensorEventConsumerThread;
import com.hust.indoorlocation.locationMethods.pdr.orientation.MagneticOrientation;
import com.hust.indoorlocation.locationMethods.pdr.step.StaticAccMagnitudeStepDetector;
import com.hust.indoorlocation.locationMethods.pdr.stride.FixedStrideLengthStrategy;
import com.hust.indoorlocation.ui.main.collector.consumer.WriteToFileConsumer;
import com.hust.indoorlocation.ui.main.collector.model.DataEvent;
import com.hust.indoorlocation.tools.util.LogUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author admin
 */
public class PDRcallbackService extends Service
implements SensorEventListener, PDRcallback
{

    private final String TAG = "PdrService";
    private SharedPreferences prefs;
    private SensorManager sensorManager;
    private final ArrayBlockingQueue sensorEventQueue = new ArrayBlockingQueue(256);
    private Thread consumer;
    private List<Sensor> supportedSensors;
    private String carryType;
    private String environment;
    private String speedLevel;
    private Integer sensorDelay=0;
    private File dir;

    public PDRcallbackService() {
    }

    @Override
    public void movement(double x, double y) {
        LogUtil.d("x="+x+";y="+y);
        sendBroadcastPDR("pdr",x,y);
    }

    public class PdrBinder extends Binder{
        /**
         * 获取当前Service的实例
         * @return
         */
        public PDRcallbackService getService(){
            return PDRcallbackService.this;
        }
    }

    public void startDownload(){

    }



    @Override
    public IBinder onBind(Intent intent) {
        return new PdrBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取系统传感器管理器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        supportedSensors=getSupportedSensors();
    }

    private  Thread pdrThread;

    private ArrayBlockingQueue dataEventQueue = new ArrayBlockingQueue<DataEvent>(256);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //每次启动service时调用
        LogUtil.d("service start:");

        //注册传感器监听者
        for (Sensor s:supportedSensors) {
            sensorManager.registerListener(this, s, sensorDelay);
        }

        long currentTimeMillis = System.currentTimeMillis();
        dir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PDR-"+String.valueOf(currentTimeMillis));
        WriteToFileConsumer.Companion.createDataDir(dir);
        WriteToFileConsumer.Companion.createFiles(supportedSensors, false, dir);
        WriteToFileConsumer.Companion.createGeneratedFiles(NaivePdrConsumer.Companion.getGeneratedList(), dir);

        CyclicBarrier barrier = new CyclicBarrier(2);
        pdrThread = new Thread(new SensorEventConsumerThread(
                 dataEventQueue,
                 barrier,
                 Arrays.asList(
                        new WriteToFileConsumer(dir),
                        new NaivePdrConsumer(
                                new StaticAccMagnitudeStepDetector(),
                                new MagneticOrientation(),
                                new FixedStrideLengthStrategy(0.5F),
                                Arrays.asList(
                                        new WriteToFileConsumer(dir)
                                ),
                                this
                        )
                )
        ));
        pdrThread.start();
        try {
            barrier.await();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        pdrThread.interrupt();

        LogUtil.d("service destroyed");
    }



    private List getSupportedSensors() {
        List<Sensor> sensorList =  Arrays.asList(
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        );
        return sensorList;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event == null) {
            return;
        }
        if(!dataEventQueue.offer(new DataEvent(DataEvent.Companion.getTYPE_SENSOR_EVENT(), event))) {
            //LogUtil.d("queue is full, event discarded");
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
            LogUtil.d("closeBt: 关闭蓝牙");
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

    private void sendBroadcastPDR(String info ,Double x,Double y) {
        Intent intent = new Intent(this.getPackageName() + ".PDR_RECEIVER");
        intent.putExtra("info", info);
        intent.putExtra("x",x);
        intent.putExtra("y",y);
        this.sendBroadcast(intent);
    }


}