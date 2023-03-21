package com.component.helper.image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView

/**
 * Created by yan_x
 * @date 2021/11/11/011 17:13
 * @description 图片加载，考虑这样封装的必要性
 * 用工具类的形式可以达到同样效果
 * 也可暴露接口对外使用，通过dagger注册
 */
object ImageLoadApp {

    fun loadImage(
        imageView: ImageView,
        url: String,
        radius: Int = 0,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    ) {
        getImageConfig()?.loadImage(
            imageView,
            url,
            radius,
            topLeftRound,
            toRightRound,
            bottomLeftRound,
            bottomRightRound
        )
    }

    fun loadImage(
        imageView: ImageView,
        url: String,
        round: Int = 0,
    ) {
        getImageConfig()?.loadImage(imageView, url, round)
    }

    fun loadImage(
        imageView: ImageView,
        bitmap: Bitmap,
        radius: Int = 0,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    ) {
        getImageConfig()?.loadImage(
            imageView,
            bitmap,
            radius,
            topLeftRound,
            toRightRound,
            bottomLeftRound,
            bottomRightRound
        )

    }

    fun loadImage(
        imageView: ImageView,
        res: Int,
        radius: Int = 0,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    ) {
        getImageConfig()?.loadImage(
            imageView,
            res,
            radius,
            topLeftRound,
            toRightRound,
            bottomLeftRound,
            bottomRightRound
        )

    }

    fun loadImage(
        imageView: ImageView,
        res: Drawable,
        radius: Int = 0,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    ) {
        getImageConfig()?.loadImage(
            imageView,
            res,
            radius,
            topLeftRound,
            toRightRound,
            bottomLeftRound,
            bottomRightRound
        )
    }

    private fun getImageConfig(): ImageStrategy? {
        return ImageLoadConfig.getImageConfig()
    }
}