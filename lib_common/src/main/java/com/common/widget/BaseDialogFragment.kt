package com.common.widget

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.lang.reflect.Field

/**
 * Created by yan_x
 * @date 2021/11/17/017 17:10
 * @description 处理dialogFragment show出现的各种异常
 */
open class BaseDialogFragment : DialogFragment() {
    override fun show(manager: FragmentManager, tag: String?) {
        showAllowingStateLoss(manager, tag)
    }

    private fun showAllowingStateLoss(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed) {
            return
        }
        if (isAdded) {
            this.dismiss()
        }
        try {
            val dismissed: Field = DialogFragment::class.java.getDeclaredField("mDismissed")
            dismissed.isAccessible = true
            dismissed.set(this, false)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        try {
            val shown: Field = DialogFragment::class.java.getDeclaredField("mShownByMe")
            shown.isAccessible = true
            shown.set(this, true)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val ft: FragmentTransaction = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }
}