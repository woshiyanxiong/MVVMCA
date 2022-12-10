package com.component.helper.loading

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.component.widget.CommonLoadingDialog
import com.component.widget.port.LoadView

/**
 * Created by yan_x
 * @date 2021/12/16/016 14:27
 * @description
 */
class CommonLoadingView : LoadView() {

    private var loadView: CommonLoadingDialog? = null

    override fun loadingShow() {
        loadView?.show()
    }

    override fun loadingDismiss() {
        loadView?.dismiss()
    }

    override fun setLifecycleObserver(lifecycle: LifecycleOwner) {
        if (loadView == null) {
            if (lifecycle is FragmentActivity) {
                loadView = CommonLoadingDialog(lifecycle)
            }

        }
    }

    override fun setHideOrShow(isShow: Boolean) {
        if (isShow) {
            loadView?.show()
        } else {
            loadView?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadView?.dismiss()
        loadView = null
        Log.e("CommonLoadingView", "onDestroy")
    }
}