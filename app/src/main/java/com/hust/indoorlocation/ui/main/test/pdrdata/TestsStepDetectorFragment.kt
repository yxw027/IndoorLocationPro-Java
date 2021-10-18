package com.hust.indoorlocation.ui.main.test.pdrdata

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.hust.indoorlocation.base.BaseFragment
import com.hust.indoorlocation.databinding.FragmentTestsStepDetectorBinding
import com.hust.indoorlocation.locationMethods.pdr.step.IStepDetector
import com.hust.indoorlocation.locationMethods.pdr.step.StaticAccMagnitudeStepDetector
import com.hust.indoorlocation.ui.main.collector.model.DataEvent
import com.hust.indoorlocation.ui.main.collector.model.DataEvent.Companion.TYPE_SENSOR_EVENT
import com.hust.indoorlocation.tools.util.SensorUtil.registerSensors


class TestsStepDetectorFragment : BaseFragment<FragmentTestsStepDetectorBinding>() , SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var stepDetector: IStepDetector
    private var stepCnt = 0

    companion object{
        const val title="步数"
        fun newInstance() = TestsStepDetectorFragment()
    }

    override fun attachViewBinding(): FragmentTestsStepDetectorBinding {
        return FragmentTestsStepDetectorBinding.inflate(layoutInflater)
    }

    override fun initialize() {
        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetector = StaticAccMagnitudeStepDetector()
    }

    override fun onResume() {
        super.onResume()
        registerSensors(
            listOf(
                Sensor.TYPE_ACCELEROMETER
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

        val e = stepDetector.updateWithDataEvent(DataEvent(TYPE_SENSOR_EVENT, event))
            ?:return
        if(stepDetector.isStepDetected()) {
            stepCnt++
            mBinding.stepCount.text = stepCnt.toString()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

//    private fun goLineGraphs(type :Int){
//        LogUtil.d("type=$type")
//        GraphsActivity.start(mContent,type = type,null)
//    }
}