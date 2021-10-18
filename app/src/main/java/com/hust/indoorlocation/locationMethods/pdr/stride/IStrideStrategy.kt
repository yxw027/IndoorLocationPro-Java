package com.hust.indoorlocation.locationMethods.pdr.stride

import com.hust.indoorlocation.ui.main.collector.model.DataEvent

interface IStrideStrategy {
    fun updateWithDataEvent(event : DataEvent) = Unit
    fun getStrideLength(): Float
    fun getLastStrideTimestamp() = 0
}