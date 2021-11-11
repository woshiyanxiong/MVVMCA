package com.mvvm.demo

import androidx.activity.viewModels
import com.common.base.BaseActivity
import com.mvvm.demo.databinding.ActivityLoginBinding
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by yan_x
 * @date 2021/11/8/008 10:16
 * @description
 */
@AndroidEntryPoint
class LoginActivity:BaseActivity<ActivityLoginBinding>() {
    private val viewModel:LoginViewModel by viewModels()
    override fun getLayout(): Int =R.layout.activity_login

    override fun initView() {

    }
}