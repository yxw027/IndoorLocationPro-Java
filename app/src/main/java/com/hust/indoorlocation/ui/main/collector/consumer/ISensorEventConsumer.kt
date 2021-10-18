package com.hust.indoorlocation.ui.main.collector.consumer

import com.hust.indoorlocation.ui.main.collector.model.DataEvent

/**
 * 传感器处理者
 */
interface ISensorEventConsumer {
    fun consume(event: DataEvent)
}