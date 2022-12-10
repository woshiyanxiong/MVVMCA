package com.component.uiStatus

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.component.result.data
import com.component.widget.CommonLoadingDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Created by yan_x
 * @date 2022/12/9/009 16:41
 * @description
 */
class UiLoadStatusManager constructor(private val lifecycleOwner: LifecycleOwner,
                                      private vararg val statusView: IStatusView) :
    DefaultLifecycleObserver {
    private var loadingDialog: CommonLoadingDialog? = null;
    private val error: String = ""

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loadingDialog = getContext()?.let { CommonLoadingDialog(it) }
        initObService()
    }

    private fun initObService() {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            statusView.toMutableSet().forEach {
                async {
                    it.loadingStatus().distinctUntilChanged().collect {
                        if (it && loadingDialog?.isShowing==true){
                            return@collect
                        }
                        showLoadingDialog(it)
                    }
                }
                async {
                    it.errorStatus().collect{
                        Log.e("当前错误","${it.data?.msg}")
                    }
                }
            }
        }
    }

    private fun showLoadingDialog(isShow:Boolean){
        if (isShow){
            loadingDialog?.show()
        }else{
            loadingDialog?.dismiss()
        }
    }

    private fun getContext(): Context? {
        return (lifecycleOwner as? AppCompatActivity) ?: (lifecycleOwner as? Fragment)?.context
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        loadingDialog?.dismiss()
    }
}