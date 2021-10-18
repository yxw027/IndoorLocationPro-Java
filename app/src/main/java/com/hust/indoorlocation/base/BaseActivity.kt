package com.hust.indoorlocation.base

import android.content.pm.ActivityInfo

import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager

import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding

import com.hust.indoorlocation.tools.util.KeyBoardUtil
import com.hust.indoorlocation.tools.util.LogUtil
import com.hust.indoorlocation.tools.util.StatusBarUtil


/**
 * @author chenxz
 * @date 2018/11/19
 * @desc BaseActivity 泛型实化 ，内部存有binding对象
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var mBinding: VB
    protected open  var mViewName: String? = this.javaClass.simpleName


    protected val mContext by lazy { this }
    /**
     * 是否使用 EventBus
     */
    open fun useEventBus(): Boolean = true

    /**
     * 返回绑定对象
     */
    abstract fun attachViewBinding(): VB

    /**
     * 1. onCreate()，创建时调用，初始化操作写在这里，如指定布局文件，成员变量绑定对应ID等。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**************************/
        //设置窗口软键盘的交互模式,保证用户要进行输入的输入框肯定在用户的视野范围里面
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 强制竖屏

        mBinding = attachViewBinding()
        setContentView(mBinding.root)

        LogUtil.d("$mViewName onCreate")
        initialize(savedInstanceState)
    }



    /**
     * 初始化函数，命名与子类BaseVmActivity初始化函数区分。
     */
    abstract fun initialize(saveInstanceState: Bundle?)


    protected fun initToolbar(toolbar: Toolbar, homeAsUpEnabled: Boolean, title: String) {
        toolbar?.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUpEnabled)
    }


    /**
     * 设置状态栏的背景颜色
     */
    fun setStatusBarColor(@ColorInt color: Int) {
        StatusBarUtil.setColor(this, color, 0)
    }

    /**
     * 设置状态栏图标的颜色
     *
     * @param dark true: 黑色  false: 白色
     */
    fun setStatusBarIcon(dark: Boolean) {
        if (dark) {
            StatusBarUtil.setLightMode(this)
        } else {
            StatusBarUtil.setDarkMode(this)
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            val v = currentFocus
            // 如果不是落在EditText区域，则需要关闭输入法
            if (KeyBoardUtil.isHideKeyboard(v, ev)) {
                KeyBoardUtil.hideKeyBoard(this, v)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 统一处理返回键，使其显示
     */
    protected fun setBackEnabled() {
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * 返回键选中处理
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        LogUtil.d(mViewName+"action id="+item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Fragment 逐个出栈
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun finish() {
        super.finish()
    }

    /**
     * 6.    onDestory()，被销毁前用，之后该Activity进入销毁状态，一般在这里释放内存。
     */
    override fun onDestroy() {
        super.onDestroy()

        CommonUtil.fixInputMethodManagerLeak(this)
       // ToastUtil.release()
    }

    /**
     * 3. onResume()，在活动准备好与用户交互时调用，此时活动一定处于栈顶，且在运行状态。
     *  onResume（）和onPause（）方法是调用比较频繁的，在这两个方法里面一般做很小耗时的操作，以增强用户体验。
     */
    override fun onResume() {
        super.onResume()
        LogUtil.d(javaClass.simpleName + " onResume")
    }

    /**
     *  4.onPause()，准备去启动或恢复另一活动时调用，当系统遇到紧急情况需要恢复内存，那么onStop()，onDestory()可能不被执行，
     *  因此你应当在onPause里保存一些至关重要的状态属性，这些属性会被保存到物理内存中。但此方法执行速度一定要快，否则会影响新栈顶活动的使用。
     */
    override fun onPause() {
        super.onPause()
        //LogUtil.d(javaClass.simpleName+" onPause")
    }

}
