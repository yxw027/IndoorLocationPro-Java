package com.hust.indoorlocation.ui.graphs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.hust.indoorlocation.R
import com.hust.indoorlocation.base.BaseLineChartFragment
import com.hust.indoorlocation.databinding.FragmentMultipleChartBinding

import com.hust.indoorlocation.tools.util.SensorUtil
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.sqrt

class LineChartMultipleFragment : BaseLineChartFragment<FragmentMultipleChartBinding>(),
    SensorEventListener{

    companion object {
        var SENSOR_TYPE:Int =0
        const val title = "多折线图"
        fun newInstance() = LineChartMultipleFragment()
    }

    override fun attachViewBinding(): FragmentMultipleChartBinding {
        return FragmentMultipleChartBinding.inflate(layoutInflater)
    }

    private lateinit var sensorManager: SensorManager

    override fun initialize() {
        setHasOptionsMenu(true)
        SENSOR_TYPE = GraphsActivity.SENSOR_TYPE

        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mFloatingActionBtn=mBinding.floatingActionBtn
        mFloatingActionBtnFast=mBinding.floatingActionBtnFast
        mFloatingActionBtn!!.setOnClickListener(fabOnClickLister)
        mFloatingActionBtnFast!!.setOnClickListener(fabFastOnClickLister)

        tvX = mBinding.tvMax
        tvY = mBinding.tvMin

        seekBarX = mBinding.seekBarMax
        seekBarX!!.setOnSeekBarChangeListener(this)
        seekBarY = mBinding.seekBarMin
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = mBinding.lineChart
        chart.setOnChartValueSelectedListener(this);
        //后台绘制
        chart.setDrawGridBackground(false);
        //设置描述文本
        chart.description.isEnabled = true;
        chart.description.text="采样周期T/s"
        //设置支持触控手势
        chart.setTouchEnabled(true);
        //设置缩放
        chart.isDragEnabled = true;
        chart.setDrawBorders(false)
        //设置推动
        chart.setScaleEnabled(true);
        //如果为false,x轴和y轴分别完成缩放；true 同时缩放
        chart.setPinchZoom(false);
        // set an alternative background color 设置背景色
        // chart.setBackgroundColor(Color.LTGRAY)

        chart.xAxis.apply {
            //设置标签字体
            typeface = tfLight
            textColor = Color.RED
            //绘制网格线
            setDrawGridLines(false)
            //绘制此轴的线
            setDrawAxisLine(false)
            //剪裁第一个和最后一个标签
            setAvoidFirstLastClipping(true)
            //x轴最大最小值
            axisMaximum = XAxisMax
            axisMinimum = XAxisMin
            //x轴在底部
            position= XAxis.XAxisPosition.BOTTOM
            //isEnabled = true
        }

        chart.axisLeft.apply {
            //  isEnabled = fals
            axisMaximum = YAxisMax
            axisMinimum = YAxisMin
            setDrawGridLines(true)
            setDrawAxisLine(false)
        }

        chart.axisRight.apply {
            isEnabled = false
        }

        seekBarX!!.progress = YAxisMax.toInt()
        seekBarY!!.progress = YAxisMin.toInt()

//        chart.data = data
//        chart.invalidate()

        //标签位置
        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
    }




    private val AXIS_X = 0
    private val AXIS_Y = 1
    private val AXIS_Z = 2
    private val AXIS_A = 3

    override fun initSensorAxis():HashMap<Int,String>{
        val s=HashMap<Int,String>()
        if(SensorUtil.infoMap.containsKey(SENSOR_TYPE)){
            s.put(AXIS_X,SensorUtil.infoMap[SENSOR_TYPE]!!.metaData[0].split("：")[0])
            s.put(AXIS_Y,SensorUtil.infoMap[SENSOR_TYPE]!!.metaData[1].split("：")[0])
            s.put(AXIS_Z,SensorUtil.infoMap[SENSOR_TYPE]!!.metaData[2].split("：")[0])
            s.put(AXIS_A,"平均值")
        }
        return s
    }

    override fun onResume() {
        super.onResume()
        if(SensorUtil.infoMap.containsKey(SENSOR_TYPE))
            SensorUtil.registerSensors(
                listOf(
                    SENSOR_TYPE
                ),
                sensorManager,
                this,
                //SENSOR_DELAY_NORMAL: 215-230 ms
                SensorManager.SENSOR_DELAY_NORMAL
            )
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        chart.resetTracking()
        if(seekBarX!!.progress<seekBarY!!.progress)
            return
        setYAxisMaxMin(max=seekBarX!!.progress.toFloat(),min=seekBarY!!.progress.toFloat())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEntry(event: SensorEvent) {
        val xTime= getCycleTime()
        addEntry(AXIS_X, xTime,getRateX(event))
        addEntry(AXIS_Y, xTime,getRateY(event))
        addEntry(AXIS_Z, xTime,getRateZ(event))
        addEntry(AXIS_A, xTime,getMagnitude(event))
    }

    override fun createLineData():LineData{
        val dataSets = ArrayList<ILineDataSet>()
        for (z in 0 until sensorAxiS.size) {
            val values = ArrayList<Entry>()
            val d = LineDataSet(values, sensorAxiS[z])
            d.lineWidth = 2.5f
            d.circleRadius = 3f
            val color = colors[z % colors.size]
            d.color = color
            d.setCircleColor(color)
            dataSets.add(d)
        }

        val data = LineData(dataSets)
        data.setValueFormatter(LargeValueFormatter())
        return data
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.line, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionToggleValues -> {
                val sets = chart.data
                    .dataSets
                for (iSet: ILineDataSet in sets) {
                    val set = iSet as LineDataSet
                    set.setDrawValues(!set.isDrawValuesEnabled)
                }
                chart.invalidate()
            }
            R.id.actionTogglePinch -> {
                if (chart.isPinchZoomEnabled) chart.setPinchZoom(false) else chart.setPinchZoom(
                    true)
                chart.invalidate()
            }
            R.id.actionToggleAutoScaleMinMax -> {
                chart.isAutoScaleMinMaxEnabled = !chart.isAutoScaleMinMaxEnabled
                chart.notifyDataSetChanged()
            }
            R.id.actionToggleHighlight -> {
                if (chart.data != null) {
                    chart.data.isHighlightEnabled = !chart.data.isHighlightEnabled
                    chart.invalidate()
                }
            }
            R.id.actionToggleFilled -> {
                val sets = chart.data
                    .dataSets
                for (iSet: ILineDataSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawFilledEnabled) set.setDrawFilled(false) else set.setDrawFilled(
                        true)
                }
                chart.invalidate()
            }
            R.id.actionToggleCircles -> {
                val sets = chart.data
                    .dataSets
                for (iSet: ILineDataSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawCirclesEnabled) set.setDrawCircles(false) else set.setDrawCircles(
                        true)
                }
                chart.invalidate()
            }
            R.id.actionToggleCubic -> {
                val sets = chart.data
                    .dataSets
                for (iSet: ILineDataSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode =
                        if (set.mode == LineDataSet.Mode.CUBIC_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.CUBIC_BEZIER
                }
                chart.invalidate()
            }
            R.id.actionToggleStepped -> {
                val sets = chart.data
                    .dataSets
                for (iSet: ILineDataSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode =
                        if (set.mode == LineDataSet.Mode.STEPPED) LineDataSet.Mode.LINEAR else LineDataSet.Mode.STEPPED
                }
                chart.invalidate()
            }
            R.id.actionToggleHorizontalCubic -> {
                val sets = chart.data
                    .dataSets
                for (iSet: ILineDataSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode =
                        if (set.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
                }
                chart.invalidate()
            }
            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveToGallery()
                } else {
                    requestStoragePermission(chart)
                }
            }
            R.id.animateX -> {
                chart.animateX(2000)
            }
            R.id.animateY -> {
                chart.animateY(2000)
            }
            R.id.animateXY -> {
                chart.animateXY(2000, 2000)
            }
        }
        return true
    }

    override fun saveToGallery() {
        saveToGallery(chart, "MultiLineChartActivity")
    }

//    //构造方法的字符格式这里如果小数不足2位,会以0补足
//    var decimalFormat: DecimalFormat = DecimalFormat(".00")

    private fun getRateX(event: SensorEvent): Float {
        return event.values[0]
    }

    private fun getRateY(event: SensorEvent): Float {
        return event.values[1]
    }

    private fun getRateZ(event: SensorEvent): Float {
        return event.values[2]
    }

    private fun getMagnitude(event: SensorEvent): Float {
        return sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                SENSOR_TYPE -> {
                    if (isPlay)
                        addEntry(event)
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    val fabFastOnClickLister =View.OnClickListener() {
        mRate=(mRate+1)%4
        sensorManager.unregisterListener(this)
        SensorUtil.registerSensors(listOf(SENSOR_TYPE),
            sensorManager,
            this,
            3 - mRate)
        when(3-mRate){
            0->mBinding.tvRateDes.text="fastest"
            1->mBinding.tvRateDes.text="faster"
            2->mBinding.tvRateDes.text="normal"
            3->mBinding.tvRateDes.text="lower"
            else->mBinding.tvRateDes.text="error unknown"
        }
    }
}