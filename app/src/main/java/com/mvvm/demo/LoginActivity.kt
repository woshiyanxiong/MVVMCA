package com.mvvm.demo

import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.common.base.BaseActivity
import com.common.ext.initLoading
import com.common.ext.navigationActivity
import com.mvvm.demo.config.AppLoadingView
import com.mvvm.demo.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint


/**
 * Created by yan_x
 * @date 2021/11/8/008 10:16
 * @description
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun getLayout(): Int = R.layout.activity_login

    override fun initView() {
        initLoading(viewModel.stateView,AppLoadingView())
        binding?.login?.setOnClickListener {
            viewModel.login(binding?.login?.text.toString(), binding?.pwd?.text.toString())
        }

        viewModel.loginSuccess.observe(this) {
            Handler(Looper.getMainLooper()).postDelayed({
                navigationActivity(MainActivity::class.java)

            },2000)
//            finish()
        }
    }
}