package com.common.widget.multistate
import android.util.Log
import android.view.View



/**
 *@author: yanx
 *@time: 2021/2/5
 *@describe: 状态view 扩展
 */

/**
 * 当状态发生改变时 隐藏某个view
 * @receiver MultiStateView
 * @param view View
 */
fun MultiStateView.changeStateGone(view: View) {
    view.visibility = View.GONE
    listener = object : MultiStateView.StateListener {
        override fun onStateChanged(viewState: MultiStateView.ViewState) {
            Log.e("VIEW状态监听", viewState.toString())
            view.visibility =
                    if (viewState != MultiStateView.ViewState.CONTENT) View.GONE else View.VISIBLE
        }

    }
}

/**
 * 显示加载状态
 * @receiver MultiStateView
 * @param noShow Boolean
 */
fun MultiStateView.showLoading() {
    this.viewState = MultiStateView.ViewState.LOADING
}


/**
 * 错误状态下的点击刷新
 * @receiver MultiStateView
 * @param onErrorClick Function0<Unit>
 */
fun MultiStateView.setError(onErrorClick: () -> Unit = {}) {
    val errorLayout = getView(MultiStateView.ViewState.ERROR)
}

/**
 *设置背景色
 * @receiver MultiStateView
 * @param color Int
 */
fun MultiStateView.setBgColor(color: Int) {
    val loadLayout = getView(MultiStateView.ViewState.LOADING)
    loadLayout?.setBackgroundResource(color)
}

/**
 * 显示有数据页面
 * @receiver MultiStateView
 */
fun MultiStateView.showContent() {
    this.viewState = MultiStateView.ViewState.CONTENT
}



