package com.component.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ImageSpan
import com.component.utils.ScreenUtils.dp2px


/**
 * @author: yanx
 * @time: 2021/2/22
 * @describe: 文字加图片
 */
class ImageTabSpan(
    var context: Context,
    resourceId: Int,
    marginRight: Float = 8f
) : ImageSpan(context, resourceId) {
    private val mMarginRight = dp2px(context, marginRight)
    override fun getSize(
        paint: Paint, text: CharSequence, start: Int, end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {

        val d = drawable
        val rect = d.bounds
        if (fm != null) {
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.bottom - fmPaint.top
            val drHeight = rect.bottom - rect.top
            val top = drHeight / 2 - fontHeight / 4
            val bottom = drHeight / 2 + fontHeight / 4
            fm.ascent = -bottom
            fm.top = -bottom
            fm.bottom = top
            fm.descent = top
        }
        return rect.right + mMarginRight
    }

    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        drawable.setBounds(0, 0, dp2px(context, 18f), dp2px(context, 18f))
        val b = drawable
        canvas.save()
        val transY: Int = (y + paint.fontMetricsInt.descent + y + paint.fontMetricsInt.ascent) / 2 - b.bounds.bottom / 2 //计算y方向的位移
        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }
}