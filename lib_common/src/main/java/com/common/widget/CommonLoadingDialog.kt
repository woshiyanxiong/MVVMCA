package com.common.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.common.R
import com.common.widget.port.LoadView

/**
 * Created by yan_x
 * @date 2021/12/15/015 10:32
 * @description 这个放到baseActivity或者其他都行
 */
class CommonLoadingDialog(context: Context, style: Int = R.style.loadingDialog) : Dialog(context, style){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_layout)
        val window: Window? = this.window
        val lp: WindowManager.LayoutParams? = window?.attributes
        lp?.gravity = Gravity.CENTER
        lp?.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        getWindow()?.attributes = lp
        window?.setBackgroundDrawableResource(R.color.transparent)
    }

    fun setShowOrHide(isShow: Boolean) {
        if (isShow) {
            if (this.isShowing) {
                dismiss()
            }
            show()
        } else {
            dismiss()
        }
    }
}