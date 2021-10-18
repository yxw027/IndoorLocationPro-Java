package com.hust.indoorlocation.tools.util

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.hust.indoorlocation.ui.main.collector.model.GeneratedType
import com.hust.indoorlocation.ui.main.survey.SensorInfo

import java.util.*

object SensorUtil {
    /**
    Accelerometer, SENSOR_DELAY_FASTEST: 18-20 ms
    Accelerometer, SENSOR_DELAY_GAME: 37-39 ms
    Accelerometer, SENSOR_DELAY_UI: 85-87 ms
    Accelerometer, SENSOR_DELAY_NORMAL: 215-230 ms

    case SENSOR_DELAY_FASTEST:
    delay = 0;
    break;
    case SENSOR_DELAY_GAME:
    delay = 20000;
    break;
    case SENSOR_DELAY_UI:
    delay = 66667;
    break;
    case SENSOR_DELAY_NORMAL:
    delay = 200000;

     不同传感器采样时间可能不同
     */

    // 兼容低版本安卓
    const val TYPE_MAGNETIC_FIELD_UNCALIBRATED = 14
    const val TYPE_GAME_ROTATION_VECTOR = 15
    const val TYPE_GYROSCOPE_UNCALIBRATED = 16
    const val TYPE_SIGNIFICANT_MOTION = 17
    const val TYPE_STEP_DETECTOR = 18
    const val TYPE_STEP_COUNTER = 19
    const val TYPE_GEOMAGNETIC_ROTATION_VECTOR = 20
    const val TYPE_HEART_RATE = 21
    const val TYPE_TILT_DETECTOR = 22
    const val TYPE_WAKE_GESTURE = 23
    const val TYPE_GLANCE_GESTURE = 24
    const val TYPE_PICK_UP_GESTURE = 25
    const val TYPE_WRIST_TILT_GESTURE = 26
    const val TYPE_DEVICE_ORIENTATION = 27
    const val TYPE_POSE_6DOF = 28
    const val TYPE_STATIONARY_DETECT = 29
    const val TYPE_MOTION_DETECT = 30
    const val TYPE_HEART_BEAT = 31
    const val TYPE_LOW_LATENCY_OFFBODY_DETECT = 34
    const val TYPE_ACCELEROMETER_UNCALIBRATED = 35

    /** 传感器信息解析  */
    var infoMap: HashMap<Int, SensorInfo> = HashMap()

    init {
        infoMap[Sensor.TYPE_ACCELEROMETER] =
            SensorInfo("加速度传感器",
                arrayOf("X 轴加速度：%.5f\n", "Y 轴加速度：%.5f\n", "Z 轴加速度：%.5f\n"))
        infoMap[Sensor.TYPE_MAGNETIC_FIELD] =
            SensorInfo("磁场传感器",
                arrayOf("X 轴加磁场强度：%.5f\n", "Y 轴磁场强度：%.5f\n", "Z 轴磁场强度：%.5f\n"))

        //过时的API，最新的获取设备方向的方法 SensorManager.getOrientation()
        infoMap[Sensor.TYPE_ORIENTATION] =
            SensorInfo("方向传感器",
                arrayOf("Z 轴的角度：%.5f\n", "X 轴的角度：%.5f\n", "Y 轴的角度：%.5f\n")) // Deprecated
        infoMap[Sensor.TYPE_GYROSCOPE] =
            SensorInfo("陀螺仪",
                arrayOf("X 轴角速度：%.5f\n", "Y 轴角速度：%.5f\n", "Z 轴角速度：%.5f\n"))
        infoMap[Sensor.TYPE_LIGHT] =
            SensorInfo("光线传感器",
                arrayOf("测量值：%.2f\n"))
        infoMap[Sensor.TYPE_PRESSURE] =
            SensorInfo("压强传感器",
                arrayOf("测量值：%.5f\n"))
        infoMap[Sensor.TYPE_TEMPERATURE] =
            SensorInfo("温度传感器",
                arrayOf("测量值：%.2f\n")) // Deprecated
        infoMap[Sensor.TYPE_PROXIMITY] =
            SensorInfo("距离传感器",
                arrayOf("测量值：%.2f\n"))
        infoMap[Sensor.TYPE_GRAVITY] =
            SensorInfo("重力传感器",
                arrayOf("X 轴加速度：%.5f\n", "Y 轴加速度：%.5f\n", "Z 轴加速度：%.5f\n"))
        infoMap[Sensor.TYPE_LINEAR_ACCELERATION] =
            SensorInfo("线性加速度传感器",
                arrayOf("X 轴加速度：%.5f\n", "Y 轴加速度：%.5f\n", "Z 轴加速度：%.5f\n"))
        infoMap[Sensor.TYPE_ROTATION_VECTOR] =
            SensorInfo("旋转矢量传感器",
                arrayOf("X 轴的角度：%.5f\n", "Y 轴的角度：%.5f\n", "Z 轴的角度：%.5f\n"))
        infoMap[Sensor.TYPE_RELATIVE_HUMIDITY] =
            SensorInfo("相对湿度传感器", arrayOf())
        infoMap[Sensor.TYPE_AMBIENT_TEMPERATURE] =
            SensorInfo("环境温度传感器", arrayOf())


        infoMap[TYPE_MAGNETIC_FIELD_UNCALIBRATED] =
            SensorInfo("磁场传感器（未校准）", arrayOf())
        infoMap[TYPE_GAME_ROTATION_VECTOR] =
            SensorInfo("旋转向量传感器（游戏用）", arrayOf())
        infoMap[TYPE_GYROSCOPE_UNCALIBRATED] =
            SensorInfo("陀螺仪（未校准）", arrayOf())
        infoMap[TYPE_SIGNIFICANT_MOTION] =
            SensorInfo("显著动作传感器", arrayOf())
        infoMap[TYPE_STEP_DETECTOR] =
            SensorInfo("计步传感器", arrayOf("读数为：%.0f\n"))
        infoMap[TYPE_STEP_COUNTER] =
            SensorInfo("步数传感器", arrayOf("测量值：%.0f\n"))
        infoMap[TYPE_GEOMAGNETIC_ROTATION_VECTOR] =
            SensorInfo("旋转矢量传感器（基于地磁场）",
                arrayOf("X 轴的角度：%.5f\n", "Y 轴的角度：%.5f\n", "Z 轴的角度：%.5f\n"))
        infoMap[TYPE_HEART_RATE] = SensorInfo("心率传感器", arrayOf())
        infoMap[TYPE_TILT_DETECTOR] =
            SensorInfo("倾斜度传感器", arrayOf()) // Hide
        infoMap[TYPE_WAKE_GESTURE] =
            SensorInfo("手势唤醒传感器", arrayOf()) // Hide
        infoMap[TYPE_GLANCE_GESTURE] =
            SensorInfo("掠过手势传感器", arrayOf()) // Hide
        infoMap[TYPE_PICK_UP_GESTURE] =
            SensorInfo("拾取动作传感器", arrayOf()) // Hide & UnsupportedAppUsage
        infoMap[TYPE_WRIST_TILT_GESTURE] =
            SensorInfo("手腕倾斜传感器", arrayOf()) // Hide & SystemApi
        infoMap[TYPE_DEVICE_ORIENTATION] =
            SensorInfo("设备方向传感器", arrayOf()) // Hide & SystemApi
        infoMap[TYPE_POSE_6DOF] =
            SensorInfo("6维姿态传感器", arrayOf())
        infoMap[TYPE_STATIONARY_DETECT] =
            SensorInfo("静止状态传感器", arrayOf())
        infoMap[TYPE_MOTION_DETECT] =
            SensorInfo("运动状态传感器", arrayOf())
        infoMap[TYPE_HEART_BEAT] = SensorInfo("心跳传感器", arrayOf())
        infoMap[TYPE_LOW_LATENCY_OFFBODY_DETECT] =
            SensorInfo("低延迟离体检测传感器", arrayOf())
        infoMap[TYPE_ACCELEROMETER_UNCALIBRATED] =
            SensorInfo("加速度传感器（未校准）", arrayOf())
    }


    fun getAllSensorType():HashMap<Int, SensorInfo>{
        return infoMap
    }

    fun containsKey(key: Any?): Boolean {
        return infoMap.containsKey(key)
    }

    fun registerSensors(
        sensorTypes: List<Int>,
        manager: SensorManager,
        listener: SensorEventListener,
        sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST
    ) {
        for(sensorType in sensorTypes) {
            val sensor = manager.getDefaultSensor(sensorType)
            sensor?.also { it ->
                manager.registerListener(listener, it, sensorDelay)
            }
        }
    }

    val generated2misc = mapOf(
        Pair(
            GeneratedType.Gen_Step_Detector,
            SensorMisc("step_detector", "timestamp", 0)
        ),
        Pair(
            GeneratedType.Gen_Rotation_Angles,
            SensorMisc("rotation_angles", "azimuth,pitch,roll,timestamp", 3)
        ),
        Pair(
            GeneratedType.Gen_Trajectory,
            SensorMisc("trajectory", "x,y,timestamp", 2)
        )
    )

    data class SensorMisc(val name: String, val firstLine: String, val size: Int)

    val sensor2misc = mapOf(
        Pair(Sensor.TYPE_ACCELEROMETER, SensorMisc("accelerometer", "x,y,z,timestamp", 3)),
        // accelerator_uncalibrated
//        Pair(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, SensorMisc("accelerometer_uncalibrated", "x,y,z,xc,yc,zc,timestamp")),
        Pair(Sensor.TYPE_GRAVITY, SensorMisc("gravity", "x,y,z,timestamp", 3)),
        Pair(Sensor.TYPE_GYROSCOPE, SensorMisc("gyroscope", "x,y,z,timestamp", 3)),
        Pair(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, SensorMisc("gyroscope_uncalibrated", "x,y,z,xc,yc,zc,timestamp", 6)),
        Pair(Sensor.TYPE_LINEAR_ACCELERATION, SensorMisc("linear_acceleration", "x,y,z,timestamp", 3)),
        Pair(Sensor.TYPE_ROTATION_VECTOR, SensorMisc("rotation_vector", "x,y,z,s,timestamp", 4)),
        Pair(Sensor.TYPE_STEP_COUNTER, SensorMisc("step_counter", "count,timestamp", 1)),
        Pair(Sensor.TYPE_STEP_DETECTOR, SensorMisc("step_detector", "timestamp", 0)),
        Pair(Sensor.TYPE_GAME_ROTATION_VECTOR, SensorMisc("game_rotation_vector", "x,y,z,timestamp", 3)),
        Pair(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, SensorMisc("geomagnetic_rotation_vector", "x,y,z,timestamp", 3)),
        Pair(Sensor.TYPE_MAGNETIC_FIELD, SensorMisc("magnetic_field", "x,y,z,timestamp", 3)),
        Pair(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, SensorMisc("magnetic_field_uncalibrated", "x,y,z,xc,yc,zc,timestamp", 3)),
        Pair(Sensor.TYPE_PROXIMITY, SensorMisc("proximity", "d,timestamp", 1)),
        Pair(Sensor.TYPE_AMBIENT_TEMPERATURE, SensorMisc("ambient_temperature", "temperature,timestamp", 1)),
        Pair(Sensor.TYPE_LIGHT, SensorMisc("light", "light,timestamp", 1)),
        Pair(Sensor.TYPE_PRESSURE, SensorMisc("pressure", "pressure,timestamp", 1)),
        Pair(Sensor.TYPE_RELATIVE_HUMIDITY, SensorMisc( "relative_humidity","humidity,timestamp", 1))
// wifi and ble are not here
    )
}