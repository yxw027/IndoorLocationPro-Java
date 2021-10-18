package com.hust.indoorlocation.ui.main.test.pdrdata

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import com.hust.indoorlocation.base.BaseFragment
import com.hust.indoorlocation.databinding.FragmentTestsAccelerationBinding
import com.hust.indoorlocation.ui.graphs.GraphsActivity
import com.hust.indoorlocation.tools.util.LogUtil
import com.hust.indoorlocation.tools.util.SensorUtil.registerSensors
import kotlin.math.sqrt


class TestsAccelerationFragment : BaseFragment<FragmentTestsAccelerationBinding>() ,
    SensorEventListener {

    companion object{
        const val title="加速度"
        fun newInstance() = TestsAccelerationFragment()
    }

    private lateinit var sensorManager: SensorManager

    override fun attachViewBinding(): FragmentTestsAccelerationBinding {
        return FragmentTestsAccelerationBinding.inflate(layoutInflater)
    }

    override fun initialize() {
        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mBinding.testLinearAccelerationText.setOnClickListener {
            goLineGraphs(Sensor.TYPE_LINEAR_ACCELERATION)
        }
        mBinding.testGravityText.setOnClickListener { goLineGraphs(Sensor.TYPE_GRAVITY) }
        mBinding.testAccelerationText.setOnClickListener {  goLineGraphs(Sensor.TYPE_ACCELEROMETER)}
    }
    override fun onResume() {
        super.onResume()
        registerSensors(
            listOf(
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_LINEAR_ACCELERATION
            ),
            sensorManager,
            this,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when(event.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    mBinding.testLinearAccelerationText.text = getTextString(event)
                }
                Sensor.TYPE_GRAVITY -> {
                    mBinding.testGravityText.text = getTextString(event)
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    mBinding.testAccelerationText.text = getTextString(event)
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun getTextString(event: SensorEvent) : String {
        return "x: ${event.values[0]}\ny: ${event.values[1]}\nz: ${event.values[2]}\n" +
                "magnitude: ${sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2])}"
    }

    private fun goLineGraphs(type :Int){
        LogUtil.d("type=$type")
        GraphsActivity.start(mContext,type = type,null)
    }
}