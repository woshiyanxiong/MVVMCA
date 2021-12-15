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

    /**
     *
     * @param imageView ImageView
     * @param url String
     * @param radius Int
     * @param topLeftRound Int
     * @param toRightRound Int
     * @param bottomLeftRound Int
     * @param bottomRightRound Int
     * 如果同时设置[radius]或[topLeftRound]等具体方位的圆角，谁优先级高看个人了
     */
    fun loadImage(
        imageView: ImageView,
        url: String,
        radius:Int=0,
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
        radius:Int=0,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    )

    fun loadImage(
        imageView: ImageView,
        @DrawableRes res: Int,
        radius:Int=0,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    )

    fun loadImage(
        imageView: ImageView,
        res: Drawable,
        radius:Int=0,
        topLeftRound: Int = 0,
        toRightRound: Int = 0,
        bottomLeftRound: Int = 0,
        bottomRightRound: Int = 0
    )
}