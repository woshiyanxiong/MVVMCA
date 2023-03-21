package com.component.widget

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.PopupWindow
import com.component.utils.ScreenUtils

/**
 * Created by yan_x
 * @date 2021/11/18/018 10:36
 * @description
 */
abstract class BasePupWindow(var context: Context?) : PopupWindow() {

    /**
     * 在某个view下显示
     * @param anchor View 显示的view
     */
    fun showAsDropDownView(anchor: View) {
        showAsDropDown(anchor)
    }

    override fun showAsDropDown(anchor: View) {
        if (Build.VERSION.SDK_INT >= 24) {
            val rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            val outRect1 = Rect()
            (anchor.context as Activity).window?.decorView?.getWindowVisibleDisplayFrame(outRect1)
            val h = outRect1.height() - rect.bottom + ScreenUtils.getStatusBarHeight(anchor.context)
            height = h
        }
        super.showAsDropDown(anchor)
    }
}