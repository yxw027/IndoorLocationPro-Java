package com.hust.mvvm.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Checkable
import android.widget.TextView
import com.just.agentweb.AgentWeb
import com.just.agentweb.DefaultWebClient
import com.hust.mvvm.widgets.CustomToast
import com.hust.indoor.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author chenxz
 * @date 2021/6/9
 * @desc toast相关拓展
 */
fun Fragment.showToast(content: String) {
    CustomToast(this?.activity?.applicationContext, content).show()
}

fun Context.showToast(content: String) {
    CustomToast(this, content).show()
}

fun Activity.showSnackMsg(msg: String) {
    val snackbar = Snackbar.make(this.window.decorView, msg, Snackbar.LENGTH_SHORT)
    val view = snackbar.view
    view.findViewById<TextView>(R.id.snackbar_text)
        .setTextColor(ContextCompat.getColor(this, R.color.white))
    snackbar.show()
}

fun Fragment.showSnackMsg(msg: String) {
    this.activity ?: return
    val snackbar = Snackbar.make(this.requireActivity().window.decorView, msg, Snackbar.LENGTH_SHORT)
    val view = snackbar.view
    view.findViewById<TextView>(R.id.snackbar_text)
        .setTextColor(ContextCompat.getColor(this.requireActivity(), R.color.white))
    snackbar.show()
}

// 扩展点击事件属性(重复点击时长)
var <T : View> T.lastClickTime: Long
    set(value) = setTag(1766613352, value)
    get() = getTag(1766613352) as? Long ?: 0

// 重复点击事件绑定
inline fun <T : View> T.setSingleClickListener(time: Long = 1000, crossinline block: (T) -> Unit) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            block(this)
        }
    }
}

/**
 * getAgentWeb
 */
fun String.getAgentWeb(
    activity: Activity,
    webContent: ViewGroup,
    layoutParams: ViewGroup.LayoutParams,
    webView: WebView,
    webViewClient: WebViewClient?,
    webChromeClient: WebChromeClient?,
    indicatorColor: Int
): AgentWeb = AgentWeb.with(activity)//传入Activity or Fragment
    .setAgentWebParent(webContent, 1, layoutParams)//传入AgentWeb 的父控件
    .useDefaultIndicator(indicatorColor, 2)// 使用默认进度条
    .setWebView(webView)
    .setWebViewClient(webViewClient as com.just.agentweb.WebViewClient?)
    .setWebChromeClient(webChromeClient as com.just.agentweb.WebChromeClient?)
    .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
    .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
    .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
    .interceptUnkownUrl()
    .createAgentWeb()//
    .ready()
    .go(this)

/**
 * 格式化当前日期
 */
fun formatCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(Date())
}

/**
 * String 转 Calendar
 */
fun String.stringToCalendar(): Calendar {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val date = sdf.parse(this)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar
}

fun dp2px(dpValue: Float): Int {
    return (0.5f + dpValue * Resources.getSystem().displayMetrics.density).toInt()
}

fun Context.openBrowser(url: String) {
    Intent(Intent.ACTION_VIEW, Uri.parse(url)).run { startActivity(this) }
}
