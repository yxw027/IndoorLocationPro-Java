package com.hust.indoorlocation.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

import com.hust.indoorlocation.R
import com.hust.indoorlocation.base.BaseActivity
import com.hust.indoorlocation.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    private val EXTRA_SHOW_FRAGMENT = "show_fragment"
    private val EXTRA_SHOW_FRAGMENT_ARGUMENTS = "show_fragment_args"
    private val EXTRA_SHOW_FRAGMENT_TITLE = "show_fragment_title"

    override fun attachViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun initialize(saveInstanceState: Bundle?) {

        val initFragment: String = intent.getStringExtra(EXTRA_SHOW_FRAGMENT) ?: ""
        val initArguments: Bundle = intent.getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS) ?: Bundle()
        val initTitle: String = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE)
            ?: resources.getString(R.string.setting)

        if (initFragment.isEmpty()) {
            //java.name是包含路径名，初始化时用
            setupFragment(SettingsFragment::class.java.name, initArguments)
        } else {
            setupFragment(initFragment, initArguments)
        }

        mBinding.toolbar.toolbarBase.apply {
            title = initTitle
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }


    private fun setupFragment(fragmentName: String, args: Bundle) {

        val c = Class.forName(fragmentName)
        val fragment: Fragment = c.newInstance() as Fragment
        //反射获得类
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
            .commit()
    }

    private fun onBuildStartFragmentIntent(
        fragmentName: String,
        args: Bundle?,
        title: String?,
    ): Intent {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClass(this, javaClass)
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentName)
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args)
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, title)
        return intent
    }


    fun startWithFragment(
        fragmentName: String, args: Bundle?,
        resultTo: Fragment?, resultRequestCode: Int, title: String?,
    ) {
        val intent = onBuildStartFragmentIntent(fragmentName, args, title)
        if (resultTo == null) {
            startActivity(intent)
        } else {
            //startActivityForResult()替代方案Activity Result Api
            resultTo.startActivityForResult(intent, resultRequestCode)
        }
    }

}