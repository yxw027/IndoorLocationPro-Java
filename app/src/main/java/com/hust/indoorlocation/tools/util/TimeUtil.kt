package com.hust.indoorlocation.tools.util

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtil {

    fun getCurrentTimeMillis():Long{
        return System.currentTimeMillis()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeMillisString():String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("ss.SS")
        return current.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeMillisFloat():Float{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("ss.SS")
        return current.format(formatter).toFloat()
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeStringFromCurrentTimeMillis(currentTimeMillis : Long) : String {
        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        return format.format(Date(currentTimeMillis))
    }

    /****
     * 时间戳转 毫秒
     * ***/
    fun timestampToTimeMillis(currentTimeMillis : Long) : Float {
        //转换秒
        val totalSeconds = currentTimeMillis / 1000

        return totalSeconds.toFloat()
    }

    /**** event.timestamp 以纳秒为单位
     * sensorEvent.timestamp是与系统启动时间相关的时间戳，若单独拿出来好像是没办法使用的。
     * 1,000 纳秒 = 1微秒 μs
     * 1,000,000 纳秒 = 1毫秒 ms
     * 1,000,000,000 纳秒 = 1秒 s
     * return ms
     **/
    fun nanoToMillis(nanoTime : Long):Long{
        return nanoTime/1000000
    }



    fun showTimestampToTime(){
        //获得系统的时间，单位为毫秒,转换为妙
        val totalMilliSeconds = System.currentTimeMillis()

        //求出现在的秒
        val totalSeconds = totalMilliSeconds / 1000
        val currentSecond = totalSeconds % 60

        //求出现在的分
        val totalMinutes = totalSeconds / 60
        val currentMinute = totalMinutes % 60
        //求出现在的小时
        val totalHour = totalMinutes / 60
        val currentHour = totalHour % 24

        //显示时间
        println("总毫秒为： $totalMilliSeconds")
        println("$currentHour:$currentMinute:$currentSecond GMT")
    }


    /**
     * 获取当前年月日
     */
    val date: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = Date()
            return sdf.format(date)
        }

    /**
     * 获取当前时分秒
     */
    val time: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = Date()
            return sdf.format(date)
        }

    /**
     * 获取当前年月日时分秒
     */
    val dateTime: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(System.currentTimeMillis())
            return sdf.format(date)
        }

    /**
     * 获取当前时间，返回Long类型
     */
    val timeForLong: Long
        get() = System.currentTimeMillis()

    /**
     * 转换为年月日
     */
    fun formatDate(mDate: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var date: Date? = null
        try {
            date = sdf.parse(mDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return sdf.format(date)
    }

}