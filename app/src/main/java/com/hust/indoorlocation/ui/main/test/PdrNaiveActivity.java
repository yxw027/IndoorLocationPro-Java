package com.hust.indoorlocation.ui.main.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hust.indoorlocation.R;
import com.hust.indoorlocation.ui.main.test.simulation.PDRcallbackService;
import com.hust.indoorlocation.tools.util.LatLngUtil;
import com.hust.indoorlocation.tools.util.LogUtil;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 */
public class PdrNaiveActivity extends AppCompatActivity implements SensorEventListener {

    // 定位相关
    private LocationClient mLocClient;
    private MyLocationListener myListener = new MyLocationListener();
    // 定位图层显示方式
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    // 初始化地图
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private UiSettings mUiSettings;

    List<LatLng> points = new ArrayList<LatLng>();
    // 是否首次定位
    private boolean isFirstLoc = true;
    // 是否开启定位图层
    private boolean isLocationLayerEnable = true;
    private MyLocationData myLocationData;
    float mCurrentZoom = 21;//默认地图缩放比例值
//    百度地图的缩放等级3.0~21.0，分别对了地图上文字比例尺为2000公里、1000公里、500公里、200公里、100公里、50公里、25公里、
//    20公里、10公里、5公里、2公里、1公里、500米、200米、100米、18-50米、19-20米、20-10米、21-5米。
    TextView infoText ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_simulation);

        infoText= (TextView) findViewById(R.id.text_Info);
        infoText.setTextColor(Color.GREEN);
        infoText.setText("pdr simulation");

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mUiSettings = mBaiduMap.getUiSettings();
        initUISettingCheckBox();
        // 获取传感器管理服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);

        // 定位初始化
        initLocation();
        initOverlay();
        setNormalType(null);
        initBottomNav();
    }

    private void initOverlay() {

    }
    boolean isSurvey=true;
    /**
     * bottom_navigation 点击事件
     */
    private void initBottomNav() {
        BottomNavigationView bottomNavigationPdr=findViewById(R.id.bottom_navigation_pdr);
        bottomNavigationPdr.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_pdr_start:
                        if(!isSurvey){
                            isSurvey=true;
                            bottomNavigationPdr.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_pause_24);
                            bottomNavigationPdr.getMenu().getItem(0).setTitle("结束");
                            points.clear();
                            points.add(last);
                         //   mMapView.getMap().clear();
                            startServer();
                        }else{
                            isSurvey=false;
                            bottomNavigationPdr.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_play_arrow_24);
                            bottomNavigationPdr.getMenu().getItem(0).setTitle("重新开始");
                            stopServer();
                        }
                        break;
                    case R.id.action_pdr_help:

                        break;

                    default:
                        break;
                }
                return false;
            }
        });
    }
    private Polyline mPolyline;//运动轨迹图层
    private BDLocation mLocation;
    private PdrReceiver receiver=new PdrReceiver();
    private LatLng last;
    public final class PdrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(@Nullable Context context, @Nullable Intent intent) {
            if (intent != null) {
                String info = intent.getStringExtra("info");
                double x=intent.getDoubleExtra("x",0);
                double y=intent.getDoubleExtra("y",0);
                LogUtil.d("---x="+x+";y="+y);
                LogUtil.d("---points="+points);
                infoText.setText("x="+x+"\ny="+y);
                if(points.isEmpty()){
                    infoText.setText("请先打开GPS定位");
                    return;
                }
                last=LatLngUtil.GetJWDB(points.get(0),x,y);

                points.add(last);
                // infoText.setText(" x="+x+"; y="+y+"\n"+ points);
                //显示当前定位点，缩放地图
                mLocation.setLongitude(last.longitude);
                mLocation.setLatitude(last.latitude);
                locateAndZoom(mLocation, last);
                mBaiduMap.setMyLocationData(myLocationData);

                //清除上一次轨迹，避免重叠绘画
                mMapView.getMap().clear();

                //起始点图层也会被清除，重新绘画
                MarkerOptions oStart = new MarkerOptions();
                oStart.position(points.get(0));
                oStart.icon(startBD);
                mBaiduMap.addOverlay(oStart);

                //将points集合中的点绘制轨迹线条图层，显示在地图上
                OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(points);
                mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
            }
        }
    }
    private MyLocationData locData;
    MapStatus.Builder builder;
    private void locateAndZoom(final BDLocation location, LatLng ll) {
        mCurrentLat = location.getLatitude();
        mCurrentLon = location.getLongitude();
        locData = new MyLocationData.Builder().accuracy(0)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mCurrentDirection).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);

        builder = new MapStatus.Builder();
        builder.target(ll).zoom(21);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
    /**
     * 定位初始化
     */
    public  void initLocation(){
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        // 打开gps
        option.setOpenGps(true);
        // 设置坐标类型
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(1000);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocClient.setLocOption(option);
        //开始定位
        mLocClient.start();

        /**
         * 添加地图缩放状态变化监听，当手动放大或缩小地图时，拿到缩放后的比例，然后获取到下次定位，
         *  给地图重新设置缩放比例，否则地图会重新回到默认的mCurrentZoom缩放比例
         */
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                mCurrentZoom = arg0.zoom;
            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    //起点图标
    BitmapDescriptor startBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_startpoint);
    //终点图标
    BitmapDescriptor finishBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_finishpoint);
    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // MapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mLocation=location;
            //获取纬度信息
            mCurrentLat = location.getLatitude();
            //获取经度信息
            mCurrentLon = location.getLongitude();
            //获取定位精度，默认值为0.0f
            mCurrentAccracy = location.getRadius();
            myLocationData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                points.add(ll);
                MapStatus.Builder builder = new MapStatus.Builder();
                //首次缩放比例尺
               // builder.target(ll).zoom(21);
                //显示当前定位点，缩放地图
                locateAndZoom(location, ll);

                //标记起点图层位置
                MarkerOptions oStart = new MarkerOptions();// 地图标记覆盖物参数配置类
                oStart.position(ll);// 覆盖物位置点，第一个点为起点
                oStart.icon(startBD);// 设置覆盖物图片
                mBaiduMap.addOverlay(oStart); // 在地图上添加此图层
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }
    public void startServer(){
        Intent intent = new Intent(this, PDRcallbackService.class);
        startService(intent);
       // bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public void stopServer(){
        Intent intent = new Intent(this, PDRcallbackService.class);
      //  unbindService(conn);
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(this.getPackageName() + ".PDR_RECEIVER");
        registerReceiver(receiver, intentFilter);
        startServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause();
        if(isSurvey) {
            stopServer();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // 在activity执行onDestroy时必须调用mMapView.onDestroy()
        mMapView.onDestroy();
    }
    /**
     * 设置定位图层的开启和关闭
     */
    public void setLocEnable(View v){
        if(isLocationLayerEnable){
            mBaiduMap.setMyLocationEnabled(false);
            ((Button) v).setText("开启定位图层");
            isLocationLayerEnable = !isLocationLayerEnable;
        }else{
            mBaiduMap.setMyLocationEnabled(true);
            mBaiduMap.setMyLocationData(myLocationData);
            ((Button) v).setText("关闭定位图层");
            isLocationLayerEnable = !isLocationLayerEnable;
        }
    }
    /**
     * 设置普通模式
     */
    public void setNormalType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 传入null，则为默认图标
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
    }

    /**
     * 设置跟随模式
     */
    public void setFollowType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    /**
     * 设置罗盘模式
     */
    public void setCompassType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            myLocationData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(mCurrentLat)
                    .longitude(mCurrentLon)
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private CheckBox mAllGesturesCB;
    private CheckBox mZoomCB;
    private CheckBox mOverlookCB;
    private CheckBox mRotateCB;
    private CheckBox mScrollCB;
    private CheckBox mDoublezoomCB;

    private void initUISettingCheckBox(){
        mAllGesturesCB = (CheckBox) findViewById(R.id.allGesture);
        mZoomCB = (CheckBox) findViewById(R.id.zoom);
        mScrollCB = (CheckBox) findViewById(R.id.scroll);
        mOverlookCB = (CheckBox) findViewById(R.id.overlook);
        mRotateCB = (CheckBox) findViewById(R.id.rotate);
        mDoublezoomCB = (CheckBox) findViewById(R.id.doublezoom);
    }

    /**
     * 是否启用缩放手势
     */
    public void setZoomEnable(View v) {
        updateGesture();
    }

    /**
     * 是否启用平移手势
     */
    public void setScrollEnable(View v) {
        updateGesture();
    }

    /**
     * 是否启用旋转手势
     */
    public void setRotateEnable(View v) {
        updateGesture();
    }

    /**
     * 是否启用俯视手势
     */
    public void setOverlookEnable(View v) {
        updateGesture();
    }

    /**
     * 是否启用指南针图层
     */
    public void setCompassEnable(View v) {
        mUiSettings.setCompassEnabled(((CheckBox) v).isChecked());
    }

    /**
     * 禁用所有手势
     */
    public void setAllGestureEnable(View v) {
        updateGesture();
    }

    /**
     * 设置双击地图按照当前地图中心点放大
     */
    public void setCenterWithDoubleClickEnable(View v){
        updateGesture();
    }

    /**
     * 更新手势状态
     */
    public void updateGesture() {
        if (mAllGesturesCB.isChecked()) {
            mUiSettings.setAllGesturesEnabled(!mAllGesturesCB.isChecked());
        } else {
            mUiSettings.setZoomGesturesEnabled(mZoomCB.isChecked());
            mUiSettings.setScrollGesturesEnabled(mScrollCB.isChecked());
            mUiSettings.setRotateGesturesEnabled(mRotateCB.isChecked());
            mUiSettings.setOverlookingGesturesEnabled(mOverlookCB.isChecked());
            mUiSettings.setEnlargeCenterWithDoubleClickEnable(mDoublezoomCB.isChecked());
        }
    }

}