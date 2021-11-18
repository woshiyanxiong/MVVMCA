package com.common.widget

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.PopupWindow
import com.common.utils.ScreenUtils

/**
 * Created by yan_x
 * @date 2021/11/18/018 10:36
 * @description
 */
abstract class BasePupWindow: PopupWindow{
    var context:Context?=null
    constructor(context: Context?){
        this.context=context
    }

    /**
     * 在某个view下显示
     * @param anchor View 显示的view
     * @param activity Activity
     */
    fun showAsDropDownView(anchor: View) {
        showAsDropDown(anchor)
    }

    override fun showAsDropDown(anchor: View) {
        if (Build.VERSION.SDK_INT >= 24) {
            val rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            val activity = anchor.context as Activity
            val outRect1 = Rect()
            (anchor.context as Activity).window?.decorView?.getWindowVisibleDisplayFrame(outRect1)
            val h = outRect1.height() - rect.bottom + ScreenUtils.getStatusBarHeight(anchor.context)
            height = h
        }
        super.showAsDropDown(anchor)
    }
}