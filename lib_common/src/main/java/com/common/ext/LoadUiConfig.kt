package com.common.ext

import androidx.fragment.app.FragmentActivity
import com.common.viewmodel.StateView
import com.common.widget.LoadingDialog


fun FragmentActivity.initLoading(state: StateView) {
    val dialog = LoadingDialog(this)
    state.isLoading.observe(this) {
        dialog.setShowOrHide(it)
    }
}