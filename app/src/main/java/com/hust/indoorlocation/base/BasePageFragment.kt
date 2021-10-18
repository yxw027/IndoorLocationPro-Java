package com.hust.indoorlocation.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


abstract class BasePageFragment<VB : ViewBinding> : BaseFragment<VB>() {

    protected val titleList = mutableListOf<String>()
    protected val fragmentList = mutableListOf<Fragment>()

    protected var mViewPager: ViewPager? = null
    protected var mTabLayout: TabLayout? = null

    protected val mViewPagerAdapter: ViewPagerAdapter by lazy {
        ViewPagerAdapter(childFragmentManager, titleList, fragmentList)
    }

    override fun initialize() {
        initPageView()

        mViewPager?.run {
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))
            adapter=mViewPagerAdapter
            offscreenPageLimit=mViewPagerAdapter.count
        }

        mTabLayout?.run {
            setupWithViewPager(mViewPager)
            // TabLayoutHelper.setUpIndicatorWidth(tabLayout)
            addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))
            addOnTabSelectedListener(onTabSelectedListener)
        }
    }

    abstract fun initPageView()

    /**
     * onTabSelectedListener
     */
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            // 默认切换的时候，会有一个过渡动画，设为false后，取消动画，直接显示
            tab?.let {
                mViewPager?.setCurrentItem(it.position, false)
            }
        }
    }
}