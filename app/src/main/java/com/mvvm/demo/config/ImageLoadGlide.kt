package com.mvvm.demo.config

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.component.helper.image.ImageStrategy

/**
 * Created by yan_x
 * @date 2021/12/15/015 11:41
 * @description
 */
class ImageLoadGlide : ImageStrategy {
    override fun loadImage(
        imageView: ImageView,
        url: String,
        radius: Int,
        topLeftRound: Int,
        toRightRound: Int,
        bottomLeftRound: Int,
        bottomRightRound: Int
    ) {
        Glide.with(imageView.context).load(url).into(imageView)
    }

    override fun loadImage(imageView: ImageView, url: String, radius: Int) {
        Glide.with(imageView.context).load(url).into(imageView)
    }

    override fun loadImage(
        imageView: ImageView,
        bitmap: Bitmap,
        radius: Int,
        topLeftRound: Int,
        toRightRound: Int,
        bottomLeftRound: Int,
        bottomRightRound: Int
    ) {
        Glide.with(imageView.context).load(bitmap).into(imageView)
    }

    override fun loadImage(
        imageView: ImageView,
        res: Int,
        radius: Int,
        topLeftRound: Int,
        toRightRound: Int,
        bottomLeftRound: Int,
        bottomRightRound: Int
    ) {
        Glide.with(imageView.context).load(res).into(imageView)
    }

    override fun loadImage(
        imageView: ImageView,
        res: Drawable,
        radius: Int,
        topLeftRound: Int,
        toRightRound: Int,
        bottomLeftRound: Int,
        bottomRightRound: Int
    ) {
        Glide.with(imageView.context).load(res).into(imageView)
    }
}