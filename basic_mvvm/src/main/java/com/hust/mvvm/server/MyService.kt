package com.hust.mvvm.server

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MyService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    //创建时调用
    override fun onCreate() {
        super.onCreate()
    }
    //每次启动service时调用
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}