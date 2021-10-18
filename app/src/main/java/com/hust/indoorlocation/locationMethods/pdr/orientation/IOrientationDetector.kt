package com.hust.indoorlocation.locationMethods.pdr.orientation

import com.hust.indoorlocation.ui.main.collector.model.DataEvent

/**
 * 输入输出定向探测器
 */
interface IOrientationDetector {
    fun updateWithDataEvent(event : DataEvent): DataEvent?
    fun getOrientation() : FloatArray
    fun lastOrientationTimestamp() : Long
}