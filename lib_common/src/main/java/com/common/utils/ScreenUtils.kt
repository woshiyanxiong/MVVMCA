package com.common.utils

import android.content.Context
import android.content.res.Resources

import java.util.*


object ScreenUtils {

    fun getStatusBarHeight(context: Context): Int {
        val resources: Resources = context.resources
        val resourceId: Int =
            resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }
}
