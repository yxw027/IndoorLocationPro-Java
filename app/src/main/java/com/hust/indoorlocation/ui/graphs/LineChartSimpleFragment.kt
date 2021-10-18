package com.hust.indoorlocation.ui.graphs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.DashPathEffect
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
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils
import com.hust.indoorlocation.R
import com.hust.indoorlocation.base.BaseLineChartFragment
import com.hust.indoorlocation.databinding.FragmentSimpleChartBinding

import com.hust.indoorlocation.tools.util.SensorUtil
import java.util.*


class LineChartSimpleFragment : BaseLineChartFragment<FragmentSimpleChartBinding>(),
    SensorEventListener {

    companion object {
        var SENSOR_TYPE: Int = 0
        const val title = "加速度"
        fun newInstance() = LineChartSimpleFragment()
    }

    override fun attachViewBinding(): FragmentSimpleChartBinding {
        return FragmentSimpleChartBinding.inflate(layoutInflater)
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


        tvX = mBinding.tvXMax
        tvY = mBinding.tvYMax

        seekBarX = mBinding.seekBar1
        seekBarX?.setOnSeekBarChangeListener(this)

        seekBarY = mBinding.seekBar2
        seekBarY?.setOnSeekBarChangeListener(this)

        chart = mBinding.lineChart
        chart.setOnChartValueSelectedListener(this)

        chart = mBinding.lineChart
        chart.setOnChartValueSelectedListener(this);
        //后台绘制
        chart.setDrawGridBackground(false);
        //设置描述文本
        chart.description.isEnabled = true;
        chart.description.text = "采样周期T/s"
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
        chart.setBackgroundColor(Color.WHITE)

        // create marker to display box when values are selected
        val mv = MyMarkerView(mContext, R.layout.custom_marker_view)
        // Set the marker to the chart
        mv.chartView = chart
        chart.marker = mv


        // // Create Limit Lines // //
        val llXAxis = LimitLine(9f, "Index 10")
        llXAxis.lineWidth = 4f
        llXAxis.enableDashedLine(10f, 10f, 0f)
        llXAxis.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        llXAxis.textSize = 10f
        llXAxis.typeface = tfRegular
        val ll1 = LimitLine(150f, "Upper Limit")
        ll1.lineWidth = 4f
        ll1.enableDashedLine(10f, 10f, 0f)
        ll1.labelPosition = LimitLabelPosition.RIGHT_TOP
        ll1.textSize = 10f
        ll1.typeface = tfRegular
        val ll2 = LimitLine(-30f, "Lower Limit")
        ll2.lineWidth = 4f
        ll2.enableDashedLine(10f, 10f, 0f)
        ll2.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        ll2.textSize = 10f
        ll2.typeface = tfRegular

        chart.xAxis.apply {
            enableGridDashedLine(10f, 10f, 0f)
            // draw limit lines behind data instead of on top
            setDrawLimitLinesBehindData(true)
            // add limit lines
            addLimitLine(ll1)
            //x轴最大最小值
            axisMaximum = XAxisMax
            axisMinimum = XAxisMin
            //x轴在底部
            position= XAxis.XAxisPosition.BOTTOM
        }

        chart.axisLeft.apply {   // // Y-Axis Style // //

            // horizontal grid lines
            enableGridDashedLine(10f, 10f, 0f)
            setDrawLimitLinesBehindData(true)
            // add limit lines
            addLimitLine(ll2)
            // axis range
            axisMaximum = YAxisMax
            axisMinimum = YAxisMin
        }
        chart.axisRight.apply {
            isEnabled = false
        }
        // add data
        seekBarX!!.progress = YAxisMax.toInt()
        seekBarY!!.progress = YAxisMin.toInt()

        //setData(45, 180f)
        // draw points over time
        chart.animateX(1500)

        // get the legend (only possible after setting data)
        //标签位置
        val l = chart.legend
        //图例性质
        l.form = LegendForm.LINE
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(true)
        l.typeface = tfLight

        l.yOffset = 0f
        l.xOffset = 10f
        l.yEntrySpace = 0f
        l.textSize = 8f
    }

    private fun setData(count: Int, range: Float) {
        val values = ArrayList<Entry>()
        for (i in 0 until count) {
            val `val` = (Math.random() * range).toFloat() - 30
            values.add(Entry(i.toFloat(), `val`, resources.getDrawable(R.drawable.star)))
        }
        val set1: LineDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, "DataSet 1")
            set1.setDrawIcons(false)

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f)

            // black lines and points
            set1.color = Color.BLACK
            set1.setCircleColor(Color.BLACK)

            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 3f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // customize legend entry
            set1.formLineWidth = 1f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f

            // text size of values
            set1.valueTextSize = 9f

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            // set the filled area
            set1.setDrawFilled(true)
            set1.fillFormatter =
                IFillFormatter { dataSet, dataProvider -> chart.axisLeft.axisMinimum }

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                val drawable = ContextCompat.getDrawable(mContext, R.drawable.fade_red)
                set1.fillDrawable = drawable
            } else {
                set1.fillColor = Color.BLACK
            }
            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets
            // create a data object with the data sets
            val data = LineData(dataSets)
            // set data
            chart.data = data
        }
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
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.setDrawValues(!set.isDrawValuesEnabled)
                }
                chart.invalidate()
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
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawFilledEnabled) set.setDrawFilled(false) else set.setDrawFilled(
                        true)
                }
                chart.invalidate()
            }
            R.id.actionToggleCircles -> {
                val sets = chart.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawCirclesEnabled) set.setDrawCircles(false) else set.setDrawCircles(
                        true)
                }
                chart.invalidate()
            }
            R.id.actionToggleCubic -> {
                val sets = chart.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode =
                        if (set.mode == LineDataSet.Mode.CUBIC_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.CUBIC_BEZIER
                }
                chart.invalidate()
            }
            R.id.actionToggleStepped -> {
                val sets = chart.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode =
                        if (set.mode == LineDataSet.Mode.STEPPED) LineDataSet.Mode.LINEAR else LineDataSet.Mode.STEPPED
                }
                chart.invalidate()
            }
            R.id.actionToggleHorizontalCubic -> {
                val sets = chart.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode =
                        if (set.mode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
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
            R.id.animateX -> {
                chart.animateX(2000)
            }
            R.id.animateY -> {
                chart.animateY(2000)
            }
            R.id.animateXY -> {
                chart.animateXY(2000, 2000)
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
        }
        return true
    }

    /****进度条改变****/
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        chart.resetTracking()
        if(seekBarX!!.progress<seekBarY!!.progress)
            return
        setYAxisMaxMin(max=seekBarX!!.progress.toFloat(),min=seekBarY!!.progress.toFloat())
//        tvX?.text = seekBarX?.progress.toString()
//        tvY?.text = seekBarY?.progress.toString()
        //setData(seekBarX!!.progress, seekBarY!!.progress.toFloat())
        // redraw
        //chart.invalidate()
    }


    private fun getRateValue(event: SensorEvent): Float {
        return event.values[0]
    }

    override fun onResume() {
        super.onResume()
        SensorUtil.registerSensors(listOf(
                SENSOR_TYPE
            ), sensorManager, this, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
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

    private val fabFastOnClickLister =View.OnClickListener() {
        mRate=(mRate+1)%4
        sensorManager.unregisterListener(this)
        SensorUtil.registerSensors(listOf(SENSOR_TYPE), sensorManager, this, 3-mRate)
        when(3-mRate){
            0->mBinding.tvRateDes.text="fastest"
            1->mBinding.tvRateDes.text="faster"
            2->mBinding.tvRateDes.text="normal"
            3->mBinding.tvRateDes.text="lower"
            else->mBinding.tvRateDes.text="error unknown"
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEntry(event: SensorEvent) {
        val xTime= getCycleTime()
        addEntry(AXIS_X, xTime,getRateValue(event))
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

        (dataSets[0] as LineDataSet).enableDashedLine(10f, 10f, 0f)
        (dataSets[0] as LineDataSet).setColors(*ColorTemplate.VORDIPLOM_COLORS)
        (dataSets[0] as LineDataSet).setCircleColors(*ColorTemplate.VORDIPLOM_COLORS)

        val data = LineData(dataSets)
        data.setValueFormatter(LargeValueFormatter())
        return data
    }


    private val AXIS_X = 0
    override fun initSensorAxis():HashMap<Int,String>{
        val s=HashMap<Int,String>()
        if(SensorUtil.infoMap.containsKey(SENSOR_TYPE)){
            s.put(AXIS_X,SensorUtil.infoMap[SENSOR_TYPE]!!.metaData[0].split("：")[0])
        }
        return s
    }

}