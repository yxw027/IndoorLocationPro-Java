package com.hust.indoorlocation.ui.main.survey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hust.indoorlocation.R;
import com.hust.indoorlocation.ui.graphs.GraphsActivity;
import com.hust.indoorlocation.tools.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author admin
 */
public class SensorSurveyActivity extends AppCompatActivity {
    // 兼容低版本安卓
    public static final int TYPE_MAGNETIC_FIELD_UNCALIBRATED = 14;
    public static final int TYPE_GAME_ROTATION_VECTOR = 15;
    public static final int TYPE_GYROSCOPE_UNCALIBRATED = 16;
    public static final int TYPE_SIGNIFICANT_MOTION = 17;
    public static final int TYPE_STEP_DETECTOR = 18;
    public static final int TYPE_STEP_COUNTER = 19;
    public static final int TYPE_GEOMAGNETIC_ROTATION_VECTOR = 20;
    public static final int TYPE_HEART_RATE = 21;
    public static final int TYPE_TILT_DETECTOR = 22;
    public static final int TYPE_WAKE_GESTURE = 23;
    public static final int TYPE_GLANCE_GESTURE = 24;
    public static final int TYPE_PICK_UP_GESTURE = 25;
    public static final int TYPE_WRIST_TILT_GESTURE = 26;
    public static final int TYPE_DEVICE_ORIENTATION = 27;
    public static final int TYPE_POSE_6DOF = 28;
    public static final int TYPE_STATIONARY_DETECT = 29;
    public static final int TYPE_MOTION_DETECT = 30;
    public static final int TYPE_HEART_BEAT = 31;
    public static final int TYPE_LOW_LATENCY_OFFBODY_DETECT = 34;
    public static final int TYPE_ACCELEROMETER_UNCALIBRATED = 35;

    /**
     * 界面元素，用于动态添加的容器
     */
    LinearLayout myList;

    /**
     * 传感器列表
     */
    List<Sensor> sensors;

    /**
     * 传感器信息解析
     */
    static HashMap<Integer, SensorInfo> infoMap;

    static {
        infoMap = new HashMap<>();
        infoMap.put(Sensor.TYPE_ACCELEROMETER, new SensorInfo("加速度传感器", new String[]{"X 轴加速度：%.5f\n", "Y 轴加速度：%.5f\n", "Z 轴加速度：%.5f\n"}));
        infoMap.put(Sensor.TYPE_MAGNETIC_FIELD, new SensorInfo("磁场传感器", new String[]{"X 轴加磁场强度：%.5f\n", "Y 轴磁场强度：%.5f\n", "Z 轴磁场强度：%.5f\n"}));
        infoMap.put(Sensor.TYPE_ORIENTATION, new SensorInfo("方向传感器", new String[]{"Z 轴的角度：%.5f\n", "X 轴的角度：%.5f\n", "Y 轴的角度：%.5f\n"}));  // Deprecated
        infoMap.put(Sensor.TYPE_GYROSCOPE, new SensorInfo("陀螺仪", new String[]{"X 轴角速度：%.5f\n", "Y 轴角速度：%.5f\n", "Z 轴角速度：%.5f\n"}));
        infoMap.put(Sensor.TYPE_LIGHT, new SensorInfo("光线传感器", new String[]{"测量值为：%.2f\n"}));
        infoMap.put(Sensor.TYPE_PRESSURE, new SensorInfo("压强传感器", new String[]{"测量值为：%.5f\n"}));
        infoMap.put(Sensor.TYPE_TEMPERATURE, new SensorInfo("温度传感器", new String[]{"测量值为：%.2f\n"}));  // Deprecated
        infoMap.put(Sensor.TYPE_PROXIMITY, new SensorInfo("距离传感器", new String[]{"测量值为：%.2f\n"}));
        infoMap.put(Sensor.TYPE_GRAVITY, new SensorInfo("重力传感器", new String[]{"X 轴加速度：%.5f\n", "Y 轴加速度：%.5f\n", "Z 轴加速度：%.5f\n"}));
        infoMap.put(Sensor.TYPE_LINEAR_ACCELERATION, new SensorInfo("线性加速度传感器", new String[]{"X 轴加速度：%.5f\n", "Y 轴加速度：%.5f\n", "Z 轴加速度：%.5f\n"}));
        infoMap.put(Sensor.TYPE_ROTATION_VECTOR, new SensorInfo("旋转矢量传感器", new String[]{"X 轴的角度：%.5f\n", "Y 轴的角度：%.5f\n", "Z 轴的角度：%.5f\n"}));
        infoMap.put(Sensor.TYPE_RELATIVE_HUMIDITY, new SensorInfo("相对湿度传感器", new String[]{}));
        infoMap.put(Sensor.TYPE_AMBIENT_TEMPERATURE, new SensorInfo("环境温度传感器", new String[]{}));
        infoMap.put(TYPE_MAGNETIC_FIELD_UNCALIBRATED, new SensorInfo("磁场传感器（未校准）", new String[]{}));
        infoMap.put(TYPE_GAME_ROTATION_VECTOR, new SensorInfo("旋转向量传感器（游戏用）", new String[]{}));
        infoMap.put(TYPE_GYROSCOPE_UNCALIBRATED, new SensorInfo("陀螺仪（未校准）", new String[]{}));
        infoMap.put(TYPE_SIGNIFICANT_MOTION, new SensorInfo("显著动作传感器", new String[]{}));
        infoMap.put(TYPE_STEP_DETECTOR, new SensorInfo("计步传感器", new String[]{"读数为：%.0f\n"}));
        infoMap.put(TYPE_STEP_COUNTER, new SensorInfo("步数传感器", new String[]{"测量值为：%.0f\n"}));
        infoMap.put(TYPE_GEOMAGNETIC_ROTATION_VECTOR, new SensorInfo("旋转矢量传感器（基于地磁场）", new String[]{"X 轴的角度：%.5f\n", "Y 轴的角度：%.5f\n", "Z 轴的角度：%.5f\n"}));
        infoMap.put(TYPE_HEART_RATE, new SensorInfo("心率传感器", new String[]{}));
        infoMap.put(TYPE_TILT_DETECTOR, new SensorInfo("倾斜度传感器", new String[]{}));  // Hide
        infoMap.put(TYPE_WAKE_GESTURE, new SensorInfo("手势唤醒传感器", new String[]{}));  // Hide
        infoMap.put(TYPE_GLANCE_GESTURE, new SensorInfo("掠过手势传感器", new String[]{}));  // Hide
        infoMap.put(TYPE_PICK_UP_GESTURE, new SensorInfo("拾取动作传感器", new String[]{}));  // Hide & UnsupportedAppUsage
        infoMap.put(TYPE_WRIST_TILT_GESTURE, new SensorInfo("手腕倾斜传感器", new String[]{}));  // Hide & SystemApi
        infoMap.put(TYPE_DEVICE_ORIENTATION, new SensorInfo("设备方向传感器", new String[]{}));  // Hide & SystemApi
        infoMap.put(TYPE_POSE_6DOF, new SensorInfo("6维姿态传感器", new String[]{}));
        infoMap.put(TYPE_STATIONARY_DETECT, new SensorInfo("静止状态传感器", new String[]{}));
        infoMap.put(TYPE_MOTION_DETECT, new SensorInfo("运动状态传感器", new String[]{}));
        infoMap.put(TYPE_HEART_BEAT, new SensorInfo("心跳传感器", new String[]{}));
        infoMap.put(TYPE_LOW_LATENCY_OFFBODY_DETECT, new SensorInfo("低延迟离体检测传感器", new String[]{}));
        infoMap.put(TYPE_ACCELEROMETER_UNCALIBRATED, new SensorInfo("加速度传感器（未校准）", new String[]{}));
    }

    /**
     * 监听器列表，用于记录正在生效的监听器
     */
    List<SensorEventListener> sensorEventListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_sensor);
        initActionBar();
        // 绑定控件
        myList = findViewById(R.id.MyList);

        //获取系统传感器管理器
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //通过系统传感器管理器..获取本机所有传感器.
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        sensorEventListeners = new ArrayList<>();

        TextView txt;
        for (Sensor s : sensors) {

            String typeString = String.format(
                    "型号：%s\n" + "类型：%s\n",
                    s.getName(),
                    infoMap.containsKey(s.getType()) ? infoMap.get(s.getType()).sensorTypeName : "未知类型"
            );
            txt = new TextView(this);
            txt.setText(typeString);
            txt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                                           goLineGraphs(s.getType());
                }
            }
            );
            myList.addView(txt);

            TextView finalTxt = txt;
            SensorEventListener listener = new SensorEventListener() {

                @Override
                public void onSensorChanged(SensorEvent event) {
                    String dataString = "";
                    if (infoMap.containsKey(event.sensor.getType())) {
                        SensorInfo info = infoMap.get(event.sensor.getType());
                        for (int i = 0; i < info.metaData.length; i++) {
                            dataString += String.format(info.metaData[i], event.values[i]);
                        }
                    }
                    finalTxt.setText(typeString + dataString);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // skip
                }
            };
            sensorManager.registerListener(listener, s, SensorManager.SENSOR_DELAY_NORMAL);
            // 将对象引用加入 List 以便 onDestroy 时销毁
            sensorEventListeners.add(listener);
        }
    }
    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarBase);
        toolbar.setTitle(this.getClass().getSimpleName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //获取系统传感器管理器
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 释放监听器
        for (SensorEventListener listener : sensorEventListeners) {
            sensorManager.unregisterListener(listener);
        }
    }

    private final void goLineGraphs(int type) {
        LogUtil.INSTANCE.d("type=" + type);
        GraphsActivity.Companion.start(this, type, (Bundle)null);
    }
}