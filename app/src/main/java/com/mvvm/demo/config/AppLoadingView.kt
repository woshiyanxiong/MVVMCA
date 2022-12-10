package com.mvvm.demo.config

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.component.widget.port.LoadView
import com.mvvm.demo.widget.LoadingDialog

/**
 * Created by yan_x
 * @date 2021/12/15/015 18:06
 * @description
 */
class AppLoadingView : LoadView() {

    private var loading: LoadingDialog? = null

    override fun loadingShow() {
        loading?.show()
    }

    override fun loadingDismiss() {
        loading?.dismiss()
    }

    override fun setLifecycleObserver(lifecycle: LifecycleOwner) {
        if (loading == null) {
            lifecycle.lifecycle.addObserver(this)
            if (lifecycle is FragmentActivity)
                loading = LoadingDialog(lifecycle)
        }
    }

    override fun setHideOrShow(isShow: Boolean) {
        if (isShow) {
            loadingShow()
        } else {
            loadingDismiss()
        }
    }

    override fun onDestroy() {
        loadingDismiss()
        loading = null
        Log.e("AppLoadingDialog", "onDestroy")
    }
}