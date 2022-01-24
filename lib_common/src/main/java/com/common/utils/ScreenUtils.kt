package com.common.utils

import android.content.Context
import android.content.res.Resources

object ScreenUtils {

    fun getStatusBarHeight(context: Context): Int {
        val resources: Resources = context.resources
        val resourceId: Int =
            resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    /**
     * dp转px
     * @param context Context
     * @param dipValue Float
     * @return Int
     */
    fun dp2px(context: Context, dipValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}
