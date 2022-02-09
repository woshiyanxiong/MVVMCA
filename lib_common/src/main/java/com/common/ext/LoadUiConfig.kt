package com.common.ext

import androidx.fragment.app.FragmentActivity
import com.common.helper.loading.CommonLoadingView
import com.common.helper.loading.LoadingConfig
import com.common.viewmodel.StateView
import com.common.widget.port.LoadView


fun FragmentActivity.initLoading(state: StateView, loadView: LoadView? = CommonLoadingView()) {
    loadView?.setLifecycleObserver(this)
    state.isLoading.observe(this) {
        loadView?.setHideOrShow(it)
    }
}