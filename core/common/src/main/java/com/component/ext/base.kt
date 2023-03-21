package com.component.ext

import android.app.Activity
import android.text.TextUtils
import android.widget.Toast



/**
 *create by 2019/12/30
 * 扩展函数基类
 *@author yx
 */
fun Activity.toast(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
    if (TextUtils.isEmpty(msg)) {
        return
    }
    Toast.makeText(this, msg, duration).show()
}

fun Activity.toast(msg: Int?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg!!, duration).show()
}


//fun<T> AutoDisposeConverter<T>.ad(pr: ScopeProvider): AutoDisposeConverter<T>? = AutoDispose.autoDisposable<T>(pr)












