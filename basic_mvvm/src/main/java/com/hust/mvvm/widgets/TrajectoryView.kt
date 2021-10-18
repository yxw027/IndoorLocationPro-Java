package com.hust.mvvm.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import com.hust.mvvm.utils.LogUtil
import java.util.*

/**
 * 轨迹 View
 * Created by ve on 2021/7/13.
 */
class TrajectoryView : View {
    /**
     * 画笔颜色数组
     */
    private val COLOR_ARRAY = intArrayOf(-0x15bccb, -0xbd7a0c,
        -0x443fb, -0xcb57ad, -0xbd42e9, -0x6f42f2, -0xe74273,
        -0xd84253, -0xdf6743, -0x569043, -0x794643, -0xc2425c)

    /**
     * 绘制画笔
     */
    private val mPaint = Paint()

    /**
     * 历史路径
     */
    private val mDrawMoveHistory: MutableList<DrawPath>? = ArrayList()

    /**
     * 用于生成随机数，随机取出颜色数组中的颜色
     */
    private val random = Random()


    private val xScale = 0
    private val yScale = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr) {
        init()
    }

    private fun init() {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = dip2px(context, 5f).toFloat()
    }

    /**
    一般而言，我们通过判断 MotionEvent 的 action 来判断输入事件类型，从而做出相应的处理。
    在不考虑多指的情况下，我们一般只关注如下几个事件类型：

    MotionEvent.ACTION_DOWN
    第一根手指点击屏幕

    MotionEvent.ACTION_UP
    最后一根手指离开屏幕

    MotionEvent.ACTION_MOVE
    屏幕上有手指在滑动

    MotionEvent.ACTION_CANCEL
    事件被拦截

    那么对于多指触控来说，除了上述常用的几种事件类型之外，我们还需要关注另外两个事件类型：

    MotionEvent.ACTION_POINTER_DOWN
    点击前屏幕上已存在手指

    MotionEvent.ACTION_POINTER_UP
    当屏幕上一根手指被抬起，此时屏幕上仍有别的手指
     * */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //多指触控需要使用 getActionMasked
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                //手指点击屏幕

                return true
            }
            MotionEvent.ACTION_MOVE -> {
                //屏幕上有手指在滑动

                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                //屏幕上有一根指头抬起，但有别的指头未抬起时的事件

            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //屏幕上已经有了手指，此时又有别的手指点击时事件
                invalidate()
            }
            MotionEvent.ACTION_UP ->                 //最后一根手指抬起，重置所有 PointerId
                clearTouchRecordStatus()
            MotionEvent.ACTION_CANCEL ->                 //事件被取消
                clearTouchRecordStatus()
        }
        return true
    }

    /**
     * 添加一个新路径，(x,y)为路径的起点，pointerId是路径的序号
     * */
    fun addNewPath(x: Float, y: Float ,pointerId :Int=0) {
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, y)
        LogUtil.d("move x=$x,y=$y")
        val drawPath = DrawPath(pointerId, pathColor, path)
        val pointList: MutableList<PointF> = ArrayList()
        pointList.add(PointF(x, y))
        pointList.add(PointF(x, y))
        drawPath.record.push(pointList)
        mDrawMoveHistory!!.add(drawPath)
    }
    fun addNewPath(x: Int, y: Int,pointerId :Int=0) {
        addNewPath(x.toFloat(),y.toFloat(),pointerId)
    }

    fun pathMoveTo(x: Int, y: Int,pointerId :Int=0){
        val pointerIndex = pointerId
        //通过 pointerIndex 获取到此次滑动事件的所有历史轨迹
        mDrawMoveHistory!![pointerId].path.lineTo(x.toFloat(), y.toFloat())
    }

    private fun addNewPath(event: MotionEvent) {
        val pointerId = event.getPointerId(event.actionIndex)
        val x = event.getX(event.findPointerIndex(pointerId))
        val y = event.getY(event.findPointerIndex(pointerId))
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, y)
        val drawPath = DrawPath(pointerId, pathColor, path)
        val pointList: MutableList<PointF> = ArrayList()
        pointList.add(PointF(x, y))
        pointList.add(PointF(x, y))
        drawPath.record.push(pointList)
        mDrawMoveHistory!!.add(drawPath)
    }

    private fun readPointList(event: MotionEvent, pointerIndex: Int): List<PointF> {
        val list: MutableList<PointF> = ArrayList()
        for (j in 0 until event.historySize) {
            list.add(PointF(event.getHistoricalX(pointerIndex, j),
                event.getHistoricalY(pointerIndex, j)))
        }
        return list
    }

    /**
     * 判断两个列表中所有的数据是否相同
     */
    private fun listEquals(lis1: List<PointF>, list2: List<PointF>): Boolean {
        if (lis1 == list2) {
            return true
        }
        if (lis1.size != list2.size) {
            return false
        }
        if (lis1.isEmpty()) {
            return true
        }
        for (i in lis1.indices) {
            val point1 = lis1[i]
            val point2 = list2[i]
            if (point1 != point2) {
                return false
            }
        }
        return true
    }

    private fun addPath(list: List<PointF>, path: Path) {
        for (item in list) {
            path.lineTo(item.x, item.y)
        }
    }

    /**
     * 清除记录触摸事件的状态
     */
    private fun clearTouchRecordStatus() {
        for (item in mDrawMoveHistory!!) {
            item.pointerId = -1
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mDrawMoveHistory == null || mDrawMoveHistory.isEmpty()) {
            return
        }
        for (item in mDrawMoveHistory) {
            mPaint.color = item.drawColor
            canvas.drawPath(item.path, mPaint)
        }
    }

    /**
     * 清空画布
     */
    fun clear() {
        mDrawMoveHistory!!.clear()
        invalidate()
    }

    /**
     * 获取绘制图案的 Bitmap
     */
    val drawBitmap: Bitmap
        get() {
            val bitmap: Bitmap
            try {
                isDrawingCacheEnabled = true
                buildDrawingCache()
                bitmap = Bitmap.createBitmap(drawingCache, 0, 0,
                    measuredWidth, measuredHeight, null, false)
            } finally {
                isDrawingCacheEnabled = false
                destroyDrawingCache()
            }
            return bitmap
        }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    private fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue.toDouble() * scale.toDouble() + 0.5).toInt()
    }

    private val pathColor: Int
        private get() = COLOR_ARRAY[random.nextInt(COLOR_ARRAY.size)]

    private class DrawPath internal constructor(pointerId: Int, drawColor: Int, path: Path) {
        /**
         * 手指 ID，默认为 -1，手指离开后置位 -1
         */
        var pointerId = -1

        /**
         * 曲线颜色
         */
        val drawColor: Int

        /**
         * 曲线路径
         */
        val path: Path

        /**
         * 轨迹列表，用于判断目标轨迹是否已添加进来
         */
        val record: Stack<List<PointF>>

        init {
            this.pointerId = pointerId
            this.drawColor = drawColor
            this.path = path
            record = Stack()
        }
    }

    companion object {
        private const val TAG = "DrawView"
    }
}