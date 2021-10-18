package com.hust.indoorlocation.ui.main.test.pdrdata

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.hust.indoorlocation.base.BaseFragment
import com.hust.indoorlocation.databinding.FragmentTestsOrientationBinding
import com.hust.indoorlocation.ui.graphs.GraphsActivity
import com.hust.indoorlocation.locationMethods.pdr.orientation.IOrientationDetector
import com.hust.indoorlocation.locationMethods.pdr.orientation.MagneticOrientation
import com.hust.indoorlocation.ui.main.collector.model.DataEvent
import com.hust.indoorlocation.ui.main.collector.model.DataEvent.Companion.TYPE_SENSOR_EVENT
import com.hust.indoorlocation.ui.main.collector.model.GeneratedEvent
import com.hust.indoorlocation.tools.util.LogUtil
import com.hust.indoorlocation.tools.util.SensorUtil.registerSensors

class TestsOrientationFragment : BaseFragment<FragmentTestsOrientationBinding>() ,
    SensorEventListener {
    companion object{
        const val title="方向"
        fun newInstance() = TestsOrientationFragment()
    }
    private lateinit var sensorManager: SensorManager
    private lateinit var orientationDetector: IOrientationDetector
    override fun attachViewBinding(): FragmentTestsOrientationBinding {
        return FragmentTestsOrientationBinding.inflate(layoutInflater)
    }

    override fun initialize() {
        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        orientationDetector = MagneticOrientation()
    }

    override fun onResume() {
        super.onResume()
        registerSensors(
            listOf(
//                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_MAGNETIC_FIELD,
                Sensor.TYPE_ACCELEROMETER,
//                Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED
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
        if(event == null) return
        val e = orientationDetector.updateWithDataEvent(DataEvent(TYPE_SENSOR_EVENT, event))
            ?: return
        val gen = e.event as GeneratedEvent
        mBinding.testRotationAnglesText.text = getTextString(gen.data)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // do nothing
    }


    private fun getTextString(data: FloatArray): String {
        return data.map { it*180f/Math.PI }.joinToString("\n") { String.format("%.2f", it) }
    }
    private fun goLineGraphs(type :Int){
        LogUtil.d("type=$type")
        GraphsActivity.start(mContext,type = type,null)
    }
}