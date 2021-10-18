package com.hust.indoorlocation.base

import android.Manifest
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.SensorManager
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.viewbinding.ViewBinding
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.hust.indoorlocation.R
import com.hust.indoorlocation.tools.util.LogUtil
import com.hust.indoorlocation.tools.util.TimeUtil

import java.util.ArrayList

abstract class BaseLineChartFragment<VB : ViewBinding> : BaseFragment<VB>() ,
    SeekBar.OnSeekBarChangeListener,
    OnChartGestureListener,
    OnChartValueSelectedListener {

    protected val tfRegular: Typeface by lazy {
        Typeface.createFromAsset(mContext.assets,
            "OpenSans-Regular.ttf")
    }
    protected  val tfLight: Typeface by lazy {
        Typeface.createFromAsset(mContext.assets,
            "OpenSans-Regular.ttf")
    }

    protected   lateinit var chart: LineChart
    protected   var seekBarX: SeekBar? = null
    protected   var seekBarY: SeekBar? = null
    protected   var tvX: TextView? = null
    protected   var tvY: TextView? = null


    val XAxisVisible=2F
    val XAxisMax=1000F
    val XAxisMin=0F
    var YAxisMax=20F
    var YAxisMin=-10F

    val colors = intArrayOf(
        ColorTemplate.VORDIPLOM_COLORS[0],
        ColorTemplate.VORDIPLOM_COLORS[1],
        ColorTemplate.VORDIPLOM_COLORS[2],
        ColorTemplate.VORDIPLOM_COLORS[3],
    )

    /****进度条改变****/
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX?.text = seekBarX?.progress.toString()
        tvY?.text = seekBarY?.progress.toString()
        
        // redraw
        chart.invalidate()
    }
    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
        LogUtil.i("Gesture", "START, x: " + me.x + ", y: " + me.y)
    }

    override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture) {
        LogUtil.i("Gesture", "END, lastGesture: $lastPerformedGesture")

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) chart.highlightValues(null) // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        LogUtil.i("LongPress", "Chart long pressed.")
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        LogUtil.i("DoubleTap", "Chart double-tapped.")
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        LogUtil.i("SingleTap", "Chart single-tapped.")
    }

    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
        LogUtil.i("Fling", "Chart fling. VelocityX: $velocityX, VelocityY: $velocityY")
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        LogUtil.i("Scale / Zoom", "ScaleX: $scaleX, ScaleY: $scaleY")
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        LogUtil.i("Translate / Move", "dX: $dX, dY: $dY")
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
        LogUtil.i("VAL SELECTED", "Value: " + e.y + ", xIndex: " + e.x
                    + ", DataSet index: " + h.dataSetIndex)
    }

    override fun onNothingSelected() {
        LogUtil.i("Nothing selected", "Nothing selected.")
    }


    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}


    protected open fun saveToGallery() {
        chart.let { saveToGallery(it, mViewName) }
    }

    protected  fun saveToGallery(chart: Chart<*>, name: String) {
        if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), 70)) Toast.makeText(
            mContext.applicationContext,
            "Saving SUCCESSFUL!",
            Toast.LENGTH_SHORT).show() else Toast.makeText(mContext.applicationContext,
            "Saving FAILED!",
            Toast.LENGTH_SHORT)
            .show()
    }

    protected  fun requestStoragePermission(view: View?) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            view?.let {
                Snackbar.make(it,
                    "Write permission is required to save image to gallery",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok
                    ) {
                        ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            DemoBase.PERMISSION_STORAGE)
                    }.show()
            }
        } else {
            Toast.makeText(mContext.applicationContext, "Permission Required!", Toast.LENGTH_SHORT)
                .show()
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                DemoBase.PERMISSION_STORAGE)
        }
    }

    var mFloatingActionBtn :FloatingActionButton ?=null
    var mFloatingActionBtnFast :FloatingActionButton ?=null
    
    var isPlay=true
    val fabOnClickLister =View.OnClickListener() {
        if(isPlay){
            isPlay=false
            mFloatingActionBtn?.apply {
                setImageResource(R.drawable.ic_baseline_pause_24)
            }
        }else {
            isPlay=true
            mFloatingActionBtn?.apply {
                setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }
    }
    var mRate = SensorManager.SENSOR_DELAY_NORMAL

    open fun setYAxisMaxMin(max:Float?,min :Float? ){
        chart.axisLeft.apply {
            if (max != null) {
                LogUtil.d("mmmmax=$max")
                YAxisMax= (max.toInt()+5).toFloat()
                tvX!!.text = "↑:${YAxisMax.toInt()}"
                axisMaximum = YAxisMax
            }
            if (min != null) {
                LogUtil.d("mmmmin=$min")
                YAxisMin= (min.toInt()-5).toFloat()
                tvY!!.text = "↓:${YAxisMin.toInt()}"
                axisMinimum = YAxisMin
            }
        }
    }
    var cycleT=0
    var forTime=0F
    var nowTime=0F
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCycleTime():Float{
        forTime=nowTime
        nowTime= TimeUtil.getTimeMillisFloat()
        if(nowTime<forTime)
            cycleT++
        return nowTime+60F*cycleT
    }

    open fun addEntry(tagAxis: Int, x: Float, y: Float) {
        if(y>YAxisMax)
            setYAxisMaxMin(max=y,YAxisMin)
        if(y<YAxisMin)
            setYAxisMaxMin(YAxisMax,min=y)

        var data = chart.data

        if(data==null){
            data=createLineData()
            chart.data=data
            chart.invalidate()
        }

        var set = data.getDataSetByIndex(tagAxis)
        // set.addEntry(...); // can be called as well
        if (set == null) {
            set = createSet()
            data.addDataSet(set)
        }
        //data.addEntry(Entry(set.entryCount.toFloat(), y), tagAxis)
        data.addEntry(Entry(x, y), tagAxis)
        data.notifyDataChanged()

        chart.notifyDataSetChanged()
        chart.setVisibleXRangeMaximum(XAxisVisible)
        //左侧移动到此位置
        chart.moveViewToX(x-XAxisVisible/2-XAxisVisible/4)
    }

    open fun createSet(): LineDataSet {
        val set = LineDataSet(null, "Dynamic Data")
        set.axisDependency = YAxis.AxisDependency.LEFT
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

    open fun createLineData(): LineData {
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

    val sensorAxiS :HashMap<Int,String> by lazy { initSensorAxis() }
    abstract fun initSensorAxis():HashMap<Int,String>
}