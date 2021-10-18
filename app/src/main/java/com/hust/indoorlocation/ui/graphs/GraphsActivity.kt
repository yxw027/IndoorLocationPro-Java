package com.hust.indoorlocation.ui.graphs

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.os.Bundle
import com.hust.indoorlocation.R
import com.hust.indoorlocation.base.BaseActivity
import com.hust.indoorlocation.databinding.ActivityGraphsBinding


class GraphsActivity : BaseActivity<ActivityGraphsBinding>() {

    companion object {
        var SENSOR_TYPE :Int = 0

        fun start(context: Context?, type: Int, bundle: Bundle? = null) {
            Intent(context, GraphsActivity::class.java).run {
                SENSOR_TYPE= type
                context?.startActivity(this, bundle)
            }
        }


    }

    override fun attachViewBinding(): ActivityGraphsBinding {
        return ActivityGraphsBinding.inflate(layoutInflater)
    }

    override fun initialize(saveInstanceState: Bundle?) {
        val toolbar = mBinding.toolbar.toolbarBase

        toolbar.run {
            title = getString(R.string.app_name)
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        val fragment = when (SENSOR_TYPE.toInt()) {
            Sensor.TYPE_ACCELEROMETER ->{//1
                toolbar.title="加速度传感器"
                LineChartMultipleFragment.newInstance()
            }
            Sensor.TYPE_MAGNETIC_FIELD-> {//2
                toolbar.title="磁场传感器"
                LineChartMultipleFragment.newInstance()
            }
            Sensor.TYPE_ORIENTATION ->{//3
                toolbar.title="方向传感器"
                LineChartMultipleFragment.newInstance()
            }
            Sensor.TYPE_LIGHT->{//5
                toolbar.title="光线传感器"
                LineChartSimpleFragment.newInstance()
            }
            Sensor.TYPE_PROXIMITY->{//8
                toolbar.title="距离传感器"
                LineChartSimpleFragment.newInstance()
            }
            Sensor.TYPE_GYROSCOPE->{//4
                toolbar.title="陀螺仪传感器"
                LineChartMultipleFragment.newInstance()
            }
            Sensor.TYPE_GRAVITY->{//9
                toolbar.title="重力传感器"
                LineChartMultipleFragment.newInstance()
            }
            Sensor.TYPE_LINEAR_ACCELERATION-> {//10
                toolbar.title="线性加速度传感器"
                LineChartMultipleFragment.newInstance()
            }
            Sensor.TYPE_ROTATION_VECTOR->{//11
                toolbar.title="旋转矢量传感器"
                LineChartMultipleFragment.newInstance()
            }
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED ->{//14
                toolbar.title="磁场传感器(未校准)"
                GraphsFragment.newInstance()
            }
            Sensor.TYPE_GAME_ROTATION_VECTOR->{//15
                toolbar.title="旋转矢量传感器(未校准)"
                GraphsFragment.newInstance()
            }
            16->{//16
                toolbar.title="陀螺仪(未校准)"
                GraphsFragment.newInstance()
            }
            17->{//17
                toolbar.title="显著动作传感器"
                GraphsFragment.newInstance()
            }
            18->{//19
                toolbar.title="步数传感器"
                GraphsFragment.newInstance()
            }
            20->{//20
                toolbar.title="旋转矢量传感器(基于地磁场)"
                LineChartMultipleFragment.newInstance()
            }
            else -> {
                toolbar.title="未知传感器(厂家定制)"
                GraphsFragment.newInstance()
            }
        }

       // toolbar.title = fragment::class.java.simpleName
        fragment ?: return
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_layout, fragment, null)
            .commit()
    }


}