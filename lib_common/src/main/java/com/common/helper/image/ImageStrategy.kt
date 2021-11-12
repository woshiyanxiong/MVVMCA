package com.common.helper.image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes

/**
 * Created by yan_x
 * @date 2021/11/11/011 17:06
 * @description 其实可以一个方法里面写完的.....
 */
interface ImageStrategy {

    fun loadImage(
        imageView: ImageView,
        url: String,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    )

    fun loadImage(
        imageView: ImageView,
        url: String,
        radius:Int
    )


    fun loadImage(
        imageView: ImageView,
        bitmap: Bitmap,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    )

    fun loadImage(
        imageView: ImageView,
        @DrawableRes res: Int,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    )

    fun loadImage(
        imageView: ImageView,
        res: Drawable,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    )
}