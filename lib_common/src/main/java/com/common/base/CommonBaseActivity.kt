package com.common.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import java.util.*

/**
 * create by 2020/5/23
 *
 * @author yx
 */
abstract class CommonBaseActivity<VB : ViewDataBinding> : AppCompatActivity() {
    var binding: VB? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<VB>(this, getLayout())
        binding?.lifecycleOwner = this
        initView()
    }

    @LayoutRes
    protected abstract fun getLayout(): Int

    protected abstract fun initView()

    //设置toolbar
    fun setSupportToolBar(toolBar: Toolbar) {
        setSupportActionBar(toolBar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
    }

    fun setTitle(title: String) {
        Objects.requireNonNull<ActionBar>(supportActionBar).title = title
    }

    override fun setTitle(title: Int) {
        Objects.requireNonNull<ActionBar>(supportActionBar).title = getString(title)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}