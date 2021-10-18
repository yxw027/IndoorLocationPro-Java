package com.hust.mvvm.widgets.behavior

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

/**
 * Created by chenxz on 2017/11/25.
 */
class ToolbarBehavior(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View>(context, attrs) {

    private var outAnimator: ObjectAnimator? = null
    private var inAnimator: ObjectAnimator? = null

    // 垂直滑动
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray
    ) {

    }
}
