package com.hust.indoorlocation.locationMethods.pdr.step

import com.hust.indoorlocation.ui.main.collector.model.DataEvent


interface IStepDetector {
    fun updateWithDataEvent(event : DataEvent): DataEvent?
    fun isStepDetected() : Boolean
    fun lastDetectedTimestamp() : Long
}