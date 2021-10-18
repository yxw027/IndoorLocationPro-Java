package com.hust.mvvm.base

/**
 * @author ve
 * @date 2021/2/26
 * @desc IView
 */
interface IVmView {


    /**
     * step 1.初始化liveData.observe
     */
    abstract fun initObserve()

    /**
     * step 2.初始化view相关, 绑定数据在此时完成
     */
    abstract fun initView()

    /**
     * step 3.初始化界面时所需要的data,从仓库获取或者网络抓取
     * activity中加载数据在初始化中完成。
     * fragment中加载数据应在lazyData中调用，不应该在初始化时调用
     */
    abstract fun initData()

    /**
     * step 4.设置监听
     */
    abstract fun initListener()

}