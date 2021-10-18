package com.hust.mvvm.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.hust.mvvm.config.AppConfig

open class App : Application() {

    companion object {
        private const val TAG="veport"
        //注释 忽略内存泄露警告
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context

    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        initApp()
    }

    private fun initApp() {
        AppConfig.init(this)
    }


}