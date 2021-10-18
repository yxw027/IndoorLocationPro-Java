package com.hust.indoorlocation.ui.main.collector.model

import android.bluetooth.le.ScanResult

data class BleEvent(val scanResult: ScanResult, val major: Int, val minor: Int)
