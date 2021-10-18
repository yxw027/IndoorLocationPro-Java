package com.hust.indoorlocation.ui.main.test.pdrdata

import android.os.Build
import androidx.annotation.RequiresApi
import com.hust.indoorlocation.base.BasePageFragment
import com.hust.indoorlocation.databinding.FragmentTestBinding


class TestFragment : BasePageFragment<FragmentTestBinding>() {

    companion object {
        fun newInstance() = TestFragment()
    }



    override fun attachViewBinding(): FragmentTestBinding {
        return FragmentTestBinding.inflate(layoutInflater)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun initPageView() {

        mViewPager=mBinding.viewPager
        mTabLayout=mBinding.tabLayout

        mViewPagerAdapter.apply {
         //   addFragment(TestsPrintFragment.newInstance(),TestsPrintFragment.title)
            addFragment(TestsAccelerationFragment.newInstance(),TestsAccelerationFragment.title)
            addFragment(TestsOrientationFragment.newInstance(),TestsOrientationFragment.title)
            addFragment(TestsStepDetectorFragment.newInstance(),TestsStepDetectorFragment.title)

        }

    }
}