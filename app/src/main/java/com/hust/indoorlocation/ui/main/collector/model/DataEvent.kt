package com.hust.indoorlocation.ui.main.collector.model

data class DataEvent(val type: Int, val event: Any){

    companion object{
        val TYPE_BLE_EVENT = 0x1
        val TYPE_SENSOR_EVENT = 0x2
        val TYPE_GENERATED_EVENT = 0x3
    }
}
