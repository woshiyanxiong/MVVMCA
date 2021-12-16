package com.common.ext

import androidx.fragment.app.FragmentActivity
import com.common.helper.loading.LoadingConfig
import com.common.viewmodel.StateView


fun FragmentActivity.initLoading(state: StateView) {
    LoadingConfig.setLifecycleObserver(this)
    state.isLoading.observe(this) {
        LoadingConfig.setHideOrShow(it)
    }
}