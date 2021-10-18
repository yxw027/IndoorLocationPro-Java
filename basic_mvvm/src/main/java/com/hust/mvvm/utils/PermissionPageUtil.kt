package com.hust.mvvm.utils

import android.os.Build
import android.text.TextUtils
import android.content.Intent
import android.content.ComponentName
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.provider.Settings
import com.hust.indoor.BuildConfig
import java.lang.Exception
import java.util.*

/**
 * @author chenxz
 * @date 2019/10/23
 * @desc 跳转到手机权限设置页面
 */
object PermissionPageUtil {
    fun gotoPermission(context: Context) {
        val brand = Build.BRAND //手机厂商
        if (TextUtils.equals(brand.lowercase(Locale.ROOT), "redmi") || TextUtils.equals(brand.lowercase(),
                "xiaomi")
        ) {
            gotoMiuiPermission(context) // 小米
        } else if (TextUtils.equals(brand.lowercase(Locale.ROOT), "meizu")) {
            gotoMeizuPermission(context)
        } else if (TextUtils.equals(brand.lowercase(),
                "huawei") || TextUtils.equals(brand.lowercase(), "honor")
        ) {
            gotoHuaweiPermission(context)
        } else {
            context.startActivity(getAppDetailSettingIntent(context))
        }
    }

    /**
     * 跳转到miui的权限管理页面
     */
    private fun gotoMiuiPermission(context: Context) {
        try { // MIUI 8
            val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
            localIntent.setClassName("com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity")
            localIntent.putExtra("extra_pkgname", context.packageName)
            context.startActivity(localIntent)
        } catch (e: Exception) {
            try { // MIUI 5/6/7
                val localIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
                localIntent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
                localIntent.putExtra("extra_pkgname", context.packageName)
                context.startActivity(localIntent)
            } catch (e1: Exception) { // 否则跳转到应用详情
                context.startActivity(getAppDetailSettingIntent(context))
            }
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private fun gotoMeizuPermission(context: Context) {
        try {
            val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            context.startActivity(getAppDetailSettingIntent(context))
        }
    }

    /**
     * 华为的权限管理页面
     */
    private fun gotoHuaweiPermission(context: Context) {
        try {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // 华为权限管理
            val comp = ComponentName("com.huawei.systemmanager",
                "com.huawei.permissionmanager.ui.MainActivity")
            intent.component = comp
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            context.startActivity(getAppDetailSettingIntent(context))
        }
    }

    /**
     * 获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）
     */
    private fun getAppDetailSettingIntent(context: Context): Intent {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        localIntent.data = Uri.fromParts("package", context.packageName, null)
        return localIntent
    }

    /**
     * 跳转到系统定位服务页面
     */
    fun gotoLocServicePage(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            intent.action = Settings.ACTION_SETTINGS
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}