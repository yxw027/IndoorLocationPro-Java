package com.hust.indoorlocation.locationMethods.ins

interface AccelerationEvent{
    // 定义一个报告 反馈的方法
    fun onAccelerationEvent(locator: Locator)
}