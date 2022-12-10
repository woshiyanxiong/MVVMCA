package com.mvvm.demo.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.mvvm.demo.R

/**
 * Created by yan_x
 * @date 2021/12/16/016 11:34
 * @description
 */
class LoadingDialog(context: Context, style: Int = com.component.R.style.loadingDialog) : Dialog(context,style){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_app)
        val window: Window? = this.window
        val lp: WindowManager.LayoutParams? = window?.attributes
        lp?.gravity = Gravity.CENTER
        lp?.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        getWindow()?.attributes = lp
        window?.setBackgroundDrawableResource(R.color.transparent)
    }
}