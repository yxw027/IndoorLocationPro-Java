package com.hust.mvvm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.telephony.TelephonyManager
import com.hust.mvvm.config.AppConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL

/**
 * @author admin
 * @date 2018/11/21
 * @desc
 */
object NetWorkUtil {

    val NETWORK_WIFI = 1 // wifi network
    val NETWORK_4G = 4 // "4G" networks
    val NETWORK_3G = 3 // "3G" networks
    val NETWORK_2G = 2 // "2G" networks
    val NETWORK_UNKNOWN = 5 // unknown network
    val NETWORK_NO = -1 // no network

    private val NETWORK_TYPE_GSM = 16
    private val NETWORK_TYPE_TD_SCDMA = 17
    private val NETWORK_TYPE_IWLAN = 18

/***************/
private const val TIMEOUT = 3000 // TIMEOUT

    /**
     * check NetworkAvailable
     *
     * @param context
     * @return
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = manager.activeNetworkInfo
        return !(null == info || !info.isAvailable)
    }

    /**
     * check NetworkConnected
     *
     * @param context
     * @return
     */
    fun isNetworkConnected(context: Context): Boolean {
        val manager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = manager.activeNetworkInfo
        return !(null == info || !info.isConnected)
    }

    /**
     * 得到ip地址
     *
     * @return
     */
    fun getLocalIpAddress(): String {
        var ret = ""
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val enumIpAddress = en.nextElement().inetAddresses
                while (enumIpAddress.hasMoreElements()) {
                    val netAddress = enumIpAddress.nextElement()
                    if (!netAddress.isLoopbackAddress) {
                        ret = netAddress.hostAddress.toString()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return ret
    }


    /**
     * ping "http://www.baidu.com"
     *
     * @return
     */
    private fun pingNetWork(): Boolean {
        var result = false
        var httpUrl: HttpURLConnection? = null
        try {
            httpUrl = URL("http://www.baidu.com").openConnection() as HttpURLConnection
            httpUrl.connectTimeout = TIMEOUT
            httpUrl.connect()
            result = true
        } catch (e: IOException) {
        } finally {
            if (null != httpUrl) {
                httpUrl.disconnect()
            }
        }
        return result
    }

    /**
     * check is3G
     *
     * @param context
     * @return boolean
     */
    @JvmStatic
    fun is3G(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_MOBILE
    }

    /**
     * isWifi
     *
     * @param context
     * @return boolean
     */
    @JvmStatic
    fun isWifi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * is2G
     *
     * @param context
     * @return boolean
     */
    @JvmStatic
    fun is2G(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return activeNetInfo != null &&
                (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EDGE ||
                        activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_GPRS ||
                        activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_CDMA)
    }

    /**
     * is wifi on
     */
    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    /**
     * 判断MOBILE网络是否可用
     */
    fun isMobile(context: Context?): Boolean {
        if (context != null) {
            //获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            //获取NetworkInfo对象
            val networkInfo = manager.activeNetworkInfo
            //判断NetworkInfo对象是否为空 并且类型是否为MOBILE
            if (networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE)
                return networkInfo.isAvailable
        }
        return false
    }
    /********************/
    /**
     * 打开网络设置界面
     *
     * 3.0以下打开设置界面
     *
     * @param context 上下文
     */
    fun openWirelessSettings(context: Context) {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        } else {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    /**
     * 获取活动网络信息
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`
     *
     * @param context 上下文
     *
     * @return NetworkInfo
     */
    @SuppressLint("MissingPermission")
    private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    /**
     * 判断网络是否连接
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`
     *
     * @return `true`: 是 `false`: 否
     */
    fun isConnected(): Boolean {
        return isConnected(AppConfig.getApplication())
    }

    /**
     * 判断网络是否连接
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`
     *
     * @return `true`: 是 `false`: 否
     */
    fun isConnected(context: Context): Boolean {
        val info = getActiveNetworkInfo(context)
        return info != null && info.isConnected
    }

    /**
     * 打开或关闭移动数据
     *
     * 需系统应用 需添加权限
     * `<uses-permission android:name="android.permission.MODIFY_PHONE_STATE"/>`
     *
     * @param context 上下文
     * @param enabled `true`: 打开 `false`: 关闭
     */
    fun setDataEnabled(context: Context, enabled: Boolean) {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val setMobileDataEnabledMethod =
                tm.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType)
            setMobileDataEnabledMethod?.invoke(tm, enabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 判断网络是否是4G
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`
     *
     * @param context 上下文
     *
     * @return `true`: 是 `false`: 否
     */
    fun is4G(context: Context): Boolean {
        val info = getActiveNetworkInfo(context)
        return info != null && info.isAvailable && info.subtype == TelephonyManager.NETWORK_TYPE_LTE
    }

    /**
     * 判断wifi是否打开
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>`
     *
     * @param context 上下文
     *
     * @return `true`: 是 `false`: 否
     */
    fun getWifiEnabled(context: Context): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    /**
     * 打开或关闭wifi
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>`
     *
     * @param context 上下文
     *
     * @param enabled true`: 打开 `false`: 关闭
     */
    fun setWifiEnabled(context: Context, enabled: Boolean) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (enabled) {
            if (!wifiManager.isWifiEnabled) {
                //				wifiManager.setWifiEnabled(true);
            }
        } else {
            if (wifiManager.isWifiEnabled) {
                //				wifiManager.setWifiEnabled(false);
            }
        }
    }

    /**
     * 判断wifi是否连接状态
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`
     *
     * @param context 上下文
     *
     * @return `true`: 连接 `false`: 未连接
     */
    fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * 获取网络运营商名称
     *
     * 中国移动、如中国联通、中国电信
     *
     * @param context 上下文
     *
     * @return 运营商名称
     */
    fun getNetworkOperatorName(context: Context): String? {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm?.networkOperatorName
    }

    /**
     * 获取当前的网络类型(WIFI,2G,3G,4G)
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`
     *
     * @param context 上下文
     *
     * @return 网络类型
     *
     *  * [.NETWORK_WIFI] = 1;
     *  * [.NETWORK_4G] = 4;
     *  * [.NETWORK_3G] = 3;
     *  * [.NETWORK_2G] = 2;
     *  * [.NETWORK_UNKNOWN] = 5;
     *  * [.NETWORK_NO] = -1;
     *
     */
    fun getNetworkType(context: Context): Int {
        var netType = NETWORK_NO
        val info = getActiveNetworkInfo(context)
        if (info != null && info.isAvailable) {

            if (info.type == ConnectivityManager.TYPE_WIFI) {
                netType = NETWORK_WIFI
            } else if (info.type == ConnectivityManager.TYPE_MOBILE) {
                when (info.subtype) {

                    NETWORK_TYPE_GSM, TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> netType =
                        NETWORK_2G

                    NETWORK_TYPE_TD_SCDMA, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> netType =
                        NETWORK_3G

                    NETWORK_TYPE_IWLAN, TelephonyManager.NETWORK_TYPE_LTE -> netType = NETWORK_4G
                    else -> {

                        val subtypeName = info.subtypeName
                        if ("TD-SCDMA".equals(subtypeName, ignoreCase = true) || "WCDMA".equals(
                                subtypeName,
                                ignoreCase = true
                            )
                            || "CDMA2000".equals(subtypeName, ignoreCase = true)
                        ) {
                            netType = NETWORK_3G
                        } else {
                            netType = NETWORK_UNKNOWN
                        }
                    }
                }
            } else {
                netType = NETWORK_UNKNOWN
            }
        }
        return netType
    }

    /**
     * 获取当前的网络类型(WIFI,2G,3G,4G)
     *
     * 依赖上面的方法
     *
     * @param context 上下文
     *
     * @return 网络类型名称
     *
     *  * NETWORK_WIFI
     *  * NETWORK_4G
     *  * NETWORK_3G
     *  * NETWORK_2G
     *  * NETWORK_UNKNOWN
     *  * NETWORK_NO
     *
     */
    fun getNetworkTypeName(context: Context): String {
        return when (getNetworkType(context)) {
            NETWORK_WIFI -> "NETWORK_WIFI"
            NETWORK_4G -> "NETWORK_4G"
            NETWORK_3G -> "NETWORK_3G"
            NETWORK_2G -> "NETWORK_2G"
            NETWORK_NO -> "NETWORK_NO"
            else -> "NETWORK_UNKNOWN"
        }
    }

    /**
     * 获取IP地址
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.INTERNET"/>`
     *
     * @param useIPv4 是否用IPv4
     *
     * @return IP地址
     */
    fun getIPAddress(useIPv4: Boolean): String? {
        try {
            val nis = NetworkInterface.getNetworkInterfaces()
            while (nis.hasMoreElements()) {
                val ni = nis.nextElement()
                // 防止小米手机返回10.0.2.15
                if (!ni.isUp) {
                    continue
                }

                val addresses = ni.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        val hostAddress = inetAddress.hostAddress
                        val isIPv4 = hostAddress.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) {
                                return hostAddress
                            }

                        } else {
                            if (!isIPv4) {
                                val index = hostAddress.indexOf('%')
                                return if (index < 0)
                                    hostAddress.toUpperCase()
                                else
                                    hostAddress.substring(0, index).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }

        return null
    }

}