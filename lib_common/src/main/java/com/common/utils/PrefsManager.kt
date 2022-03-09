package com.common.utils

import android.content.Context
import com.tencent.mmkv.MMKV


/**
 * Created by yan_x
 * @date 2021/11/17/017 17:09
 * @description kv
 */
object PrefsManager {

    private val kv = MMKV.defaultMMKV()

    fun initKv(context: Context) {
        MMKV.initialize(context)
    }

    fun putString(key: String, value: String) {
        kv.putString(key, value)
    }

    fun getString(key: String): String {
        return kv.getString(key, "") ?: ""
    }
}