package com.hust.mvvm.widgets

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.hust.indoor.R
import com.hust.indoor.databinding.DialogCustomBinding



/**
 * @author chenxz
 * @date 2019/11/15
 * @desc 自定义 Dialog
 */

class CustomDialog : DialogFragment() {

    companion object {
        val TITLE = "title"
        val CONTENT = "content"
        val LEFT_TEXT = "left_text"
        val RIGHT_TEXT = "right_text"
        val CANCEL_ABLE = "cancel_able"

        fun newInstance(builder: Builder): CustomDialog {
            val bundle = Bundle()
            val fragment = CustomDialog()
            bundle.putString(TITLE, builder.title)
            bundle.putString(CONTENT, builder.content)
            bundle.putString(LEFT_TEXT, builder.leftText)
            bundle.putString(RIGHT_TEXT, builder.rightText)
            fragment.arguments = bundle
            fragment.isCancelable = builder.cancelAble
            return fragment
        }

        fun newBuilder(): Builder {
            return Builder()
        }
    }

    private var onConfirmClickListener: (() -> Unit)? = null
    private var onCancelClickListener: (() -> Unit)? = null

    fun setOnConfirmClickListener(click: (() -> Unit)): CustomDialog {
        this.onConfirmClickListener = click
        return this
    }

    fun setOnCancelClickListener(click: (() -> Unit)): CustomDialog {
        this.onCancelClickListener = click
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return@OnKeyListener true
            }
            false
        })

        val rootView = inflater.inflate(R.layout.dialog_custom, container, false)
        initView(rootView)
        return rootView
    }

    private fun initView(view: View) {
        val mBinding= DialogCustomBinding.bind(view)

        val title = this.arguments?.getString(TITLE)
        if (title.isNullOrEmpty().not()) {
            mBinding.tvTitle.visibility = View.VISIBLE
            mBinding.tvTitle.text = title
        }

        val content = this.arguments?.getString(CONTENT)
        if (content.isNullOrEmpty().not()) {
            mBinding.tvContent.visibility = View.VISIBLE
            mBinding.tvContent.text = content
        }

        val leftText = this.arguments?.getString(LEFT_TEXT)
        if (leftText.isNullOrEmpty().not()) {
            mBinding.tvCancel.visibility = View.VISIBLE
            mBinding.tvCancel.text = leftText
        } else {
            mBinding.tvCancel.visibility = View.GONE
        }

        val rightText = this.arguments?.getString(RIGHT_TEXT)
        if (rightText.isNullOrEmpty().not()) {
            mBinding.tvConfirm.visibility = View.VISIBLE
            mBinding.tvConfirm.text = rightText
        } else {
            mBinding.tvConfirm.visibility = View.GONE
        }

        // 如果左边按钮和右边按钮都显示，则分割线显示
        if (mBinding.tvCancel.visibility == View.VISIBLE &&
            mBinding.tvConfirm.visibility == View.VISIBLE
        ) {
            mBinding.viewDivider.visibility = View.VISIBLE
        } else {
            mBinding.viewDivider.visibility = View.GONE
        }

        mBinding.tvConfirm.setOnClickListener(object : OnNoDoubleClickListener() {
            override fun onNoDoubleClick(v: View?) {
                onConfirmClickListener?.invoke()
                dismiss()
            }
        })
        mBinding.tvCancel.setOnClickListener(object : OnNoDoubleClickListener() {
            override fun onNoDoubleClick(v: View?) {
                onCancelClickListener?.invoke()
                dismiss()
            }
        })
    }

    class Builder {
        private var mConfirmDialog: CustomDialog? = null
        var title: String = ""
        var content: String = ""
        var leftText: String = ""
        var rightText: String = ""
        var cancelAble: Boolean = false

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setContent(content: String): Builder {
            this.content = content
            return this
        }

        fun setLeftText(leftText: String): Builder {
            this.leftText = leftText
            return this
        }

        fun setRightText(rightText: String): Builder {
            this.rightText = rightText
            return this
        }

        fun setCancelAble(cancelAble: Boolean): Builder {
            this.cancelAble = cancelAble
            return this
        }

        fun build(): CustomDialog? {
            if (this.mConfirmDialog == null) {
                this.mConfirmDialog = newInstance(this)
            }
            return this.mConfirmDialog
        }
    }

    /**
     * @author admin
     * @date 2018/11/23
     * @desc 防止连续点击
     */
    abstract class OnNoDoubleClickListener : View.OnClickListener {

        private var mThrottleFirstTime: Long = 1000
        private var mLastClickTime: Long = 0

        constructor()
        constructor(throttleFirstTime: Long) {
            mThrottleFirstTime = throttleFirstTime
        }

        override fun onClick(v: View?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - mLastClickTime > mThrottleFirstTime) {
                mLastClickTime = currentTime
                onNoDoubleClick(v)
            }
        }

        abstract fun onNoDoubleClick(v: View?)

    }
}

