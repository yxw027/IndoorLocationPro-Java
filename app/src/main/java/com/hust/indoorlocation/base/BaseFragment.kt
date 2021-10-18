package com.hust.indoorlocation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.hust.indoorlocation.tools.util.LogUtil


/**
 * @author chenxz
 * @date 2018/11/19
 * @desc BaseFragment
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment(){


    /** 当前fragment的视图绑定 */
    protected open var _binding: VB? = null
    protected open val mBinding get() = _binding!!
    protected open var mViewName: String = this.javaClass.simpleName
    /** 当前fragment运行的父类activity上下文*/
    protected val mContext:Context by lazy { requireActivity() }

    /**
     * 数据是否加载过了
     */
    private var isLoadData = false


    /**
     * 是否使用 EventBus
     */
    open fun useEventBus(): Boolean = false

    /**
     * 懒加载数据,数据的初始化请在此处完成
     */
    open fun lazyLoadData() {

    }
    /**
     * 无网状态—>有网状态 的自动重连操作，子类可重写该方法
     */
    open fun doReConnected() {
        lazyLoadData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = attachViewBinding()
        return mBinding.root
    }

    /**
     * 得到绑定对象
     */
    abstract fun attachViewBinding(): VB

    /**
     * 视图创建完毕
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LogUtil.d("$mViewName onCreate")
        initialize()
    }


    abstract fun initialize()

    /**
     * 懒加载，当Fragment显示的时候再请求数据
     * 如果数据不需要每次都刷新，可以先判断数据是否存在
     * 不存在 -> 请求数据，存在 -> 什么都不做
     */
    override fun onResume() {
        super.onResume()
        // 实现懒加载
        if (!isLoadData) {
            lazyLoadData()
            isLoadData = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        LogUtil.d(javaClass.simpleName+" onDestroy")
    }


}