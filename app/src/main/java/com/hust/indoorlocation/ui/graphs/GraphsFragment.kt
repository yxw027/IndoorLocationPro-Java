package com.hust.indoorlocation.ui.graphs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.hust.indoorlocation.R
import com.hust.indoorlocation.base.BaseFragment
import com.hust.indoorlocation.databinding.FragmentGraphsBinding
import com.hust.indoorlocation.tools.util.SensorUtil

import kotlin.math.sqrt


class GraphsFragment : BaseFragment<FragmentGraphsBinding>() , SensorEventListener,
    OnChartValueSelectedListener  {


    companion object{
        const val title="加速度"
        fun newInstance() = GraphsFragment()
    }

    override fun attachViewBinding(): FragmentGraphsBinding {
        return FragmentGraphsBinding.inflate(layoutInflater)
    }
    private val tfRegular: Typeface by lazy { Typeface.createFromAsset( mContext.assets, "OpenSans-Regular.ttf") }
    private val tfLight: Typeface  by lazy { Typeface.createFromAsset( mContext.assets, "OpenSans-Regular.ttf") }


    private lateinit var sensorManager: SensorManager
    private lateinit var chart:LineChart

    override fun initialize() {
        setHasOptionsMenu(true)
        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        chart = mBinding.lineChart

        //设置手势滑动事件
       // chart.setOnChartGestureListener(this);
        //设置数值选择监听
        chart.setOnChartValueSelectedListener(this);
        //后台绘制
        chart.setDrawGridBackground(false);
        //设置描述文本
        chart.description.isEnabled = true;
        //设置支持触控手势
        chart.setTouchEnabled(true);
        //设置缩放
        chart.isDragEnabled = true;
        //设置推动
        chart.setScaleEnabled(true);
        //如果禁用,扩展可以在x轴和y轴分别完成
        chart.setPinchZoom(true);


        // set an alternative background color 设置背景色
        chart.setBackgroundColor(Color.LTGRAY)

        val data = LineData()
        data.setValueTextColor(Color.WHITE)
        // add empty data
        chart.data = data


        // get the legend (only possible after setting data) 获取图例（仅在设置数据后才可能）
        val l = chart.legend

        // modify the legend ...
        l.form = LegendForm.LINE
        l.typeface = tfLight
        l.textColor = Color.WHITE

        val xl = chart.xAxis
        xl.typeface = tfLight
        xl.textColor = Color.WHITE
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true

        val leftAxis = chart.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.textColor = Color.WHITE
        leftAxis.axisMaximum = 30f
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(true)

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        //feedMultiple()
    }

    override fun onResume() {
        super.onResume()

        SensorUtil.registerSensors(
            listOf(
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_LINEAR_ACCELERATION
            ),
            sensorManager,
            this,
            //game每秒钟取值20~25次
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        if (thread != null) {
            thread!!.interrupt()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.realtime, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionAdd -> {
                addEntry()
            }
            R.id.actionClear -> {
                chart.clearValues()
                Toast.makeText(mContext, "Chart cleared!", Toast.LENGTH_SHORT).show()
            }
            R.id.actionFeedMultiple -> {
                feedMultiple()
            }
            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveToGallery()
                } else {

                }
            }
        }
        return true
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when(event.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                   // addEntry(getMagnitude(event))
                   // mBinding.testLinearAccelerationText.text = getTextString(event)
                }
                Sensor.TYPE_GRAVITY -> {
                   // mBinding.testGravityText.text = getTextString(event)
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    addEntry(getMagnitude(event))
                  //  mBinding.testAccelerationText.text = getTextString(event)
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

    private fun getMagnitude(event: SensorEvent) : Float {
        return sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2])
      }

    private fun addEntry() {
        val data = chart.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }
            data.addEntry(Entry(set.entryCount.toFloat(), (Math.random() * 40).toFloat() + 30f ), 0)
            data.notifyDataChanged()

            // let the chart know it's data has changed
            chart.notifyDataSetChanged()

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120f)
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.entryCount.toFloat())

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private fun addEntry(y:Float) {
        val data = chart.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }
            data.addEntry(Entry(set.entryCount.toFloat(), y ), 0)
            data.notifyDataChanged()

            chart.notifyDataSetChanged()
            chart.setVisibleXRangeMaximum(120f)
            chart.moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "Dynamic Data")
        set.axisDependency = AxisDependency.LEFT
        set.color = ColorTemplate.getHoloBlue()
        set.setCircleColor(Color.WHITE)
        set.lineWidth = 2f
        set.circleRadius = 4f
        set.fillAlpha = 65
        set.fillColor = ColorTemplate.getHoloBlue()
        set.highLightColor = Color.rgb(244, 117, 117)
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 9f
        set.setDrawValues(false)
        return set
    }

    private var thread: Thread? = null

    private fun feedMultiple() {
        if (thread != null) thread!!.interrupt()

        thread = Thread {
            for (i in 0..999) {
                // Don't generate garbage runnables inside the loop.
                    addEntry()
                try {
                    Thread.sleep(25)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        thread!!.start()
    }


     fun saveToGallery() {
        saveToGallery(chart, "RealtimeLineChartActivity")
    }

    override fun onValueSelected(e: Entry, h: Highlight?) {
        Log.i("Entry selected", e.toString())
    }

    override fun onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.")
    }

    private fun saveToGallery(chart: Chart<*>, name: String) {
        if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), 70)) Toast.makeText(
            mContext.applicationContext,
            "Saving SUCCESSFUL!",
            Toast.LENGTH_SHORT).show() else Toast.makeText( mContext.applicationContext,
            "Saving FAILED!",
            Toast.LENGTH_SHORT)
            .show()
    }

}