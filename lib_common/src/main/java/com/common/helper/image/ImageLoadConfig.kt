package com.common.helper.image

import android.util.Log

/**
 * Created by yan_x
 * @date 2021/12/16/016 16:14
 * @description 图片加载配置
 */
object ImageLoadConfig {
    private var imageConfig: ImageStrategy? = null

    fun init(imageConfig: ImageStrategy) {
        this.imageConfig = imageConfig
    }

    fun getImageConfig(): ImageStrategy? {
        if (imageConfig == null) {
            Log.e("ImageLoad", "ImageLoad未初始化")
        }
        return imageConfig
    }
}