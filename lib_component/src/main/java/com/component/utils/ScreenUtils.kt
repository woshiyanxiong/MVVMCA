package com.component.utils


import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager


object ScreenUtils {

    /**
     * 获取顶部状态栏高度
     * @param context Context
     * @return Int
     */
    fun getStatusBarHeight(context: Context): Int {
        val resources: Resources = context.resources
        val resourceId: Int =
            resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 获取屏幕宽度
     * @param context Context
     * @return Int
     */
    fun getScreenWidth(context: Context): Int {
        val wm = context.applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
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

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }


    /**
     * 获取虚拟按键的高度
     *      1. 全面屏下
     *          1.1 开启全面屏开关-返回0
     *          1.2 关闭全面屏开关-执行非全面屏下处理方式
     *      2. 非全面屏下
     *          2.1 没有虚拟键-返回0
     *          2.1 虚拟键隐藏-返回0
     *          2.2 虚拟键存在且未隐藏-返回虚拟键实际高度
     */
    fun getNavigationBarHeightIfRoom(context: Context): Int {
        return if (navigationGestureEnabled(context)) {
            0
        } else getCurrentNavigationBarHeight(context as Activity)
    }

    /**
     * 全面屏（是否开启全面屏开关 0 关闭  1 开启）
     *
     * @param context
     * @return
     */
    private fun navigationGestureEnabled(context: Context): Boolean {
        val `val`: Int = Settings.Global.getInt(context.contentResolver, getDeviceInfo(), 0)
        return `val` != 0
    }

    /**
     * 获取设备信息（目前支持几大主流的全面屏手机，亲测华为、小米、oppo、魅族、vivo都可以）
     *
     * @return
     */
    private fun getDeviceInfo(): String {
        val brand: String = Build.BRAND
        if (TextUtils.isEmpty(brand)) return "navigationbar_is_min"
        return if (brand.equals("HUAWEI", ignoreCase = true)) {
            "navigationbar_is_min"
        } else if (brand.equals("XIAOMI", ignoreCase = true)) {
            "force_fsg_nav_bar"
        } else if (brand.equals("VIVO", ignoreCase = true)) {
            "navigation_gesture_on"
        } else if (brand.equals("OPPO", ignoreCase = true)) {
            "navigation_gesture_on"
        } else {
            "navigationbar_is_min"
        }
    }

    /**
     * 非全面屏下 虚拟键实际高度(隐藏后高度为0)
     * @param activity
     * @return
     */
    private fun getCurrentNavigationBarHeight(activity: Activity): Int {
        return if (isNavigationBarShown(activity)) {
            getNavigationBarHeight(activity)
        } else {
            0
        }
    }

    /**
     * 非全面屏下 虚拟按键是否打开
     * @param activity
     * @return
     */
    private fun isNavigationBarShown(activity: Activity): Boolean {
        //虚拟键的view,为空或者不可见时是隐藏状态
        val view: View = activity.findViewById(android.R.id.navigationBarBackground) ?: return false
        val visible: Int = view.visibility
        return !(visible == View.GONE || visible == View.INVISIBLE)
    }

    /**
     * 非全面屏下 虚拟键高度(无论是否隐藏)
     * @param context
     * @return
     */
    private fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


}
