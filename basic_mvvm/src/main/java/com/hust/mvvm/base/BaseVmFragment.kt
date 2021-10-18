package com.hust.mvvm.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.hust.mvvm.ext.showToast

/**
 * @author chenxz
 * @date 2019/11/1
 * @desc BaseVMFragment
 */
abstract class BaseVmFragment<VB : ViewBinding, VM : ViewModel> : BaseFragment<VB>(), IView  ,IVmView{

    lateinit var mViewModel: VM

    override fun initialize() {
        setHasOptionsMenu(true)

        mViewModel =ViewModelProvider(this).get(attachViewModelClass())
        startObserver()

        initObserve()
        initView()
        initListener()

       // initData()
    }


    abstract fun attachViewModelClass(): Class<VM>

    override fun lazyLoadData() {
        super.lazyLoadData()
        initData()
    }

    /**
     * 订阅，有逻辑的话，复写的时候super不要去掉
     */
    open fun startObserver() {

    }

    override fun initListener() {

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
        showToast("加载中")
    }

    override fun hideLoading() {
    }

}