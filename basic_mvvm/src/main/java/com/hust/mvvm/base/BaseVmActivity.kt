package com.hust.mvvm.base

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.cxz.multipleview.MultipleStatusView
import com.hust.mvvm.ext.showToast

/**
 * @author chenxz
 * @date 2019/11/1
 * @desc BaseVMActivity
 */
abstract class BaseVmActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB>(),IView ,IVmView {

    lateinit var mViewModel: VM

    /**
     * 获取ViewModel的class
     */
    protected abstract fun attachViewModelClass(): Class<VM>

    /**
     * 提示View
     */
    protected lateinit var mTipView: View
    protected lateinit var mWindowManager: WindowManager
    protected lateinit var mLayoutParams: WindowManager.LayoutParams

    protected var mLayoutStatusView: MultipleStatusView? = null

    /**
     * 是否需要显示 TipView
     */
    open fun enableNetworkTip(): Boolean = true

    /**
     * 基类订阅，有逻辑的话，复写的时候super不要去掉
     */
    open fun baseObserver() {
        // 需要登录，跳转登录页
        mViewModel.needLogin.observe(this, {
            //showMsg("请先登录账户")
        })
    }

    override fun initialize(saveInstanceState: Bundle?) {
        mViewModel = ViewModelProvider(this).get(attachViewModelClass())
        baseObserver()

        initObserve()
        initView()
        initListener()
        initData()
        mLayoutStatusView?.setOnClickListener(mRetryClickListener)
    }

    open val mRetryClickListener: View.OnClickListener = View.OnClickListener {
        mLayoutStatusView?.showContent()
    }


    /**
     * 在initView初始化后修改view
     * */
    override fun initListener() {

    }

    override fun onDestroy() {
        lifecycle.removeObserver(mViewModel)
        super.onDestroy()
    }

    override fun showMsg(msg: String) {
        showToast(msg)
    }

    override fun showDefaultMsg(msg: String) {
        showToast(msg)
    }

    override fun showError(errorMsg: String) {
        showToast(errorMsg)
    }

    override fun showLoading() {

    }

    override fun hideLoading() {
    }

}