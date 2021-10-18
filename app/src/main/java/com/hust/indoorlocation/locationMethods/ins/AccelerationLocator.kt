package com.hust.indoorlocation.locationMethods.ins

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.hust.indoorlocation.tools.util.LogUtil
import com.hust.indoorlocation.tools.util.TimeUtil
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 根据加速度和时间的乘积，求物体当前位置
 * **/
class AccelerationLocator(sensorManager: SensorManager) : SensorEventListener {

    private val sensorType = Sensor.TYPE_LINEAR_ACCELERATION
    private var sensorDelay = SensorManager.SENSOR_DELAY_NORMAL
    private var mSensorManager: SensorManager? = sensorManager

    lateinit var eventListener: AccelerationEvent
    private var x = 0
    private var y = 0

    private var xMistake = 0F
    private var yMistake = 0F

    private var xTotalDistance = 0F
    private var yTotalDistance = 0F
    private var totalTime = 0F
    private var xTotalRate = 0F
    private var yTotalRate = 0F


    private var forTimestamp = 0L
    private var nowTimestamp = 0L
    private var disTime = 0L
    var isPrint = true

    init {
        mSensorManager = sensorManager
        val sensor = mSensorManager!!.getDefaultSensor(sensorType)
        sensor?.also { it ->
            mSensorManager!!.registerListener(this, it, sensorDelay)
        }
    }

    private var xcount = 0F
    private var ycount = 0F

    fun addAccelEvent(event: SensorEvent) {
        //  a mm/ms², t ms , v mm/ms
        var xAccel = getAccelX(event) * 1000 / (1000 * 1000)
        var yAccel = getAccelY(event) * 1000 / (1000 * 1000)
        var time = getTimeMillis(event) // ms

        //第一次是错误的时间，返回
        if (time > 2000)
            return
        //x轴加速度校正
        if (abs(xAccel) < (0.4 / 1000)) {
            xMistake += xAccel
            xcount++
            xAccel = 0F
        } else
            xAccel -= (xMistake / xcount)
        //y轴加速度校正
        if (abs(yAccel) < (0.4 / 1000)) {
            yMistake += yAccel
            ycount++
            yAccel = 0F
        } else
            yAccel -= (yMistake / ycount)
        LogUtil.d(" x acc=${getAccelX(event)} ")
        LogUtil.d(" y acc=$xAccel ")
        LogUtil.d(" t acc=$time")

        xTotalRate += (xAccel * time)
        yTotalRate += (yAccel * time)
//        LogUtil.d(" x rate=$xTotalRate += ($xAccel * $time) ")
//        LogUtil.d(" y rate=$yTotalRate += ($yAccel * $time) ")
        var xDistance = getDistanceMm(xAccel, time, xTotalRate)
        var yDistance = getDistanceMm(yAccel, time, yTotalRate)


        eventListener.onAccelerationEvent(Locator(xDistance, yDistance, time))
        xTotalDistance += xDistance
        yTotalDistance += yDistance
        totalTime += time
//
//        if(isPrint){
//            LogUtil.d("all x=$xTotalDistance")
//            LogUtil.d("all y=$yTotalDistance")
//            LogUtil.d("all t=$totalTime")
//        }
    }

    /**** 加速度求位移公式 x=(v1+v0)/2 * t = v0t+1/2·at²m **/
    fun getDistanceMm(a: Float, sTime: Float, eTime: Float, v0: Float = 0F): Float {
        val t = eTime - sTime
        return getDistanceMm(a, t, v0)
    }

    /**** 加速度求位移公式 x=(v1+v0)/2 * t = v0t+1/2·at²
     * 单位 a cm/ms², t ms , v cm/ms
     * a m/s²=1/(1000*1000) m/ms² = 1000/(1000*1000) mm/ms²
     * return mm
     * 注意，整形1/2=0
     **/
    private fun getDistanceMm(a: Float, t: Float, v0: Float = 0F): Float {
        val distance = (v0 * t) + (1.0F / 2) * (a * t * t)
        LogUtil.d("distance:$distance=$v0 * $t + (1 / 2) * $a * $t * $t")
        return distance
    }


    fun getDistance(a: Float, sTime: Float, eTime: Float, v0: Float = 0F): Float {
        val t = eTime - sTime
        return getDistanceMm(a, t, v0)
    }

    /***加速度求位移公式  x=(v1+v0)/2 * t =v0t+1/2·at²
     * 单位 a m/s², t s , v m/s
     * return m
     * **/
    private fun getDistance(a: Float, t: Float, v0: Float = 0F): Float {
        val distance = v0 * t + (1.0F / 2) * a * t * t
        // LogUtil.d("distance:$distance=$v0 * $t + (1 / 2) * $a * $t * $t")
        return distance
    }

    /**** event.timestamp 以纳秒为单位
     * sensorEvent.timestamp是与系统启动时间相关的时间戳，若单独拿出来好像是没办法使用的。
    1,000,000 纳秒 = 1毫秒 ms
    return ms
     **/
    private fun getTimeMillis(event: SensorEvent): Float {
        forTimestamp = nowTimestamp
        nowTimestamp = event.timestamp
        //时间戳之差，单位为纳秒
        disTime = nowTimestamp - forTimestamp

//        LogUtil.d("for : =$forTimestamp")
//        LogUtil.d("now : =$nowTimestamp")
//        LogUtil.d("disTime = $disTime")

        return TimeUtil.nanoToMillis(disTime).toFloat()
    }

    private fun getAccelX(event: SensorEvent): Float {
        return event.values[0]
    }

    private fun getAccelY(event: SensorEvent): Float {
        return event.values[1]
    }

    private fun getRateZ(event: SensorEvent): Float {
        return event.values[2]
    }

    private fun getRateXY(event: SensorEvent): Float {
        return sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1])
    }

    private fun getDirectionXY(event: SensorEvent): Float {
        return sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1])
    }

    private fun getRateXYZ(event: SensorEvent): Float {
        return sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
    }

    private fun getMagnitude(event: SensorEvent): Float {
        return sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                sensorType -> {
                    addAccelEvent(event)
                }
                else -> {

                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    fun setAccelerationEvent(eventLister: AccelerationEvent) {
        this.eventListener = eventLister
    }

    fun release() {
        mSensorManager?.unregisterListener(this)
    }
}