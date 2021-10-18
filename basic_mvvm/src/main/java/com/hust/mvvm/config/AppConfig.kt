package com.hust.mvvm.config

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hust.mvvm.utils.DisplayManager
import com.hust.mvvm.utils.LogUtil
import com.yechaoa.yutilskt.YUtils

/**
 * @author chenxz
 * @date 2019/11/1
 * @desc 配置文件，用来初始化项目所需要的配置
 */
object AppConfig {

    const val TAG = "mmsg"
    //控制三方库的编译模式：开启日志
    const val isDebug = true

    private lateinit var application:Application
    fun getApplication(): Application {
        return application
    }


    /**
     * Init, it must be call before used .
     */
    fun init(application: Application) {
        this.application = application

        initUiMode()
        initYUtils()
        DisplayManager.init(application)
        //收集日志错误信息
        CrashHandler.instance.init(application)
    }

    private fun initUiMode() {
        //脱离系统设置，强制让当前应用程序使用浅色主题
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun initYUtils() {
        YUtils.init(application)
        LogUtil.setIsLog(isDebug, "mmsg")
    }
}