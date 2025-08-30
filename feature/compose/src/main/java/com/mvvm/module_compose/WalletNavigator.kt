package com.mvvm.module_compose

import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter

object WalletNavigator {
    
    /**
     * 跳转到钱包导入页面
     */
    fun navigateToImport(context: Context) {
        ARouter.getInstance()
            .build("/wallet/import")
            .navigation(context)
    }
    
    /**
     * 跳转到钱包主页
     */
    fun navigateToMain(context: Context) {
        ARouter.getInstance()
            .build("/wallet/main")
            .navigation(context)
    }
    
    /**
     * 跳转到设置页面
     */
    fun navigateToSettings(context: Context) {
        ARouter.getInstance()
            .build("/wallet/settings")
            .navigation(context)
    }
}