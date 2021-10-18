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
 * 画板 View
 *
 *
 * Created by zk721 on 2018/2/17.
 */
class DrawView : View {
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //多指触控需要使用 getActionMasked
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                //处理点击事件
                performClick()
                //重置所有 PointerId 为 -1
                clearTouchRecordStatus()
                //新增一个轨迹
                addNewPath(event)
                //重绘
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (mDrawMoveHistory!!.size > 0) {
                    var i = 0
                    while (i < event.pointerCount) {

                        //遍历当前屏幕上所有手指
                        val itemPointerId = event.getPointerId(i) //获取到这个手指的 ID
                        for (itemPath in mDrawMoveHistory) {
                            //遍历绘制记录表，通过 ID 找到对应的记录
                            if (itemPointerId == itemPath.pointerId) {
                                val pointerIndex = event.findPointerIndex(itemPointerId)
                                //通过 pointerIndex 获取到此次滑动事件的所有历史轨迹
                                val recordList = readPointList(event, pointerIndex)
                                if (!listEquals(recordList, itemPath.record.peek())) {
                                    //判断该 List 是否已存在，不存在则添加进去
                                    itemPath.record.push(recordList)
                                    addPath(recordList, itemPath.path)
                                }
                            }
                        }
                        i++
                    }
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                //屏幕上有一根指头抬起，但有别的指头未抬起时的事件
                val pointerId = event.getPointerId(event.actionIndex)
                for (item in mDrawMoveHistory!!) {
                    if (item.pointerId == pointerId) {
                        //该手指已绘制结束，将此 PointerId 重置为 -1
                        item.pointerId = -1
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //屏幕上已经有了手指，此时又有别的手指点击时事件
                addNewPath(event)
                invalidate()
            }
            MotionEvent.ACTION_UP ->                 //最后一根手指抬起，重置所有 PointerId
                clearTouchRecordStatus()
            MotionEvent.ACTION_CANCEL ->                 //事件被取消
                clearTouchRecordStatus()
        }
        return true
    }

    private fun addNewPath(event: MotionEvent) {
        val pointerId = event.getPointerId(event.actionIndex)
        val x = event.getX(event.findPointerIndex(pointerId))
        val y = event.getY(event.findPointerIndex(pointerId))
        LogUtil.d("move x=$x,y=$y")
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