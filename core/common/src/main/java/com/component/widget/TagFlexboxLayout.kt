package com.component.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.component.R
import com.component.utils.ScreenUtils.dp2px
import com.google.android.flexbox.FlexLine
import com.google.android.flexbox.FlexboxLayout


/**
 * Created by yan_x
 * @date 2021/1/28/031 14:55
 * @description 标签_布局
 */
class TagFlexboxLayout : FlexboxLayout {

    /**
     * 数据源
     */
    private var data = arrayListOf<String>()

    /**
     * 标签背景
     */
    private var tagBg = R.drawable.shape_tag

    /**
     * 标签文字颜色
     */
    private var tagTextColor = R.color.color_60000000

    /**
     * 标签文字大小
     */
    private var tagTextSize = 12

    /**
     * 标签文字左右padding
     */
    private var tagPadding = 8f

    /**
     * 间距
     */
    private var tagSpacing = 8f

    private var tagView: View? = null

    /**
     * 最大行数超出截断
     */
    private var maxLines = 1

    /**
     * 点击事件
     */
    private var itemClick: (key: String, position: Int) -> Unit = { _, _ -> }


    constructor(context: Context) : super(context) {
        initView()
    }

    @SuppressLint("CustomViewStyleable", "ResourceAsColor")
    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        @SuppressLint("Recycle")
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagFlexboxLayout)
        val drawable = typedArray.getResourceId(
            R.styleable.TagFlexboxLayout_tagBackground,
            R.drawable.shape_tag
        )
        maxLines = typedArray.getInt(R.styleable.TagFlexboxLayout_tagMaxLine, NOT_SET)
        drawable.let {
            tagBg = it
        }
        tagTextColor = typedArray.getResourceId(
            R.styleable.TagFlexboxLayout_tagTextColor,
            R.color.color_60000000
        )
        tagTextSize = typedArray.getDimensionPixelSize(
            R.styleable.TagFlexboxLayout_tagTextSize,
            dp2px(context, 12f)
        )
        tagPadding = typedArray.getDimension(R.styleable.TagFlexboxLayout_tagPadding, 8f)
        tagSpacing = typedArray.getDimension(R.styleable.TagFlexboxLayout_tagSpacing, 8f)
        initView()

    }

    private fun initView() {
        tagView = getTagTextView()
    }

    private fun setTagView() {
        this.removeAllViews()
        data.forEachIndexed { index, s ->
            this.addView(getAddView(s, index))
        }
    }

    private fun getAddView(text: String, position: Int): View {
        val view = getTagTextView()
        setTagViewData(view, text, position)
        return view
    }


    private fun setTagViewData(view: View, text: String, position: Int) {
        val textView = view.findViewById<TextView>(R.id.tvTag)
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(context, tagTextColor))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tagTextSize.toFloat())
        textView.setBackgroundResource(tagBg)
        textView.setPadding(getPadding(), dp2px(context, 2f), getPadding(), dp2px(context, 2f))
        textView.setOnClickListener {
            itemClick.invoke(text, position)
        }
    }

    private fun getPadding(): Int {
        return tagPadding.toInt()
    }

    override fun getMaxLine(): Int {
        return NOT_SET
    }

    override fun setMaxLine(maxLine: Int) {
        maxLines = maxLine
    }

    fun getMaxLines(): Int {
        return maxLines
    }

    override fun getFlexLinesInternal(): MutableList<FlexLine> {
        val flexLines = super.getFlexLinesInternal()
        val size = flexLines.size
        if (maxLines in 1 until size) {
            flexLines.subList(maxLines, size).clear()
        }
        return flexLines
    }

    /**
     * 获取标签布局
     * @return View 标签
     */
    @SuppressLint("InflateParams")
    private fun getTagTextView(): View {
        return LayoutInflater.from(context).inflate(R.layout.widget_layout_flex, null, false)
    }

    fun setItemOnClick(itemClick: ((key: String, position: Int) -> (Unit))) {
        this.itemClick = itemClick
    }

    fun setData(data: ArrayList<String>) {
        this.data.clear()
        this.data.addAll(data)
        setTagView()
    }
}

@BindingAdapter(value = ["TageFlexboxData"])
fun setTageFlexboxTagData(view: TagFlexboxLayout, data: List<String>?) {
    data?.let {
        view.setData(it as ArrayList<String>)
    }

}