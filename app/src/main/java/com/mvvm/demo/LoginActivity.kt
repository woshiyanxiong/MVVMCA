package com.mvvm.demo

import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.component.base.BaseActivity
import com.component.ext.navigationActivity
import com.component.uiStatus.IUiLoadStatus
import com.mvvm.demo.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * Created by yan_x
 * @date 2021/11/8/008 10:16
 * @description
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun getLayout(): Int = R.layout.activity_login

    @Inject
    lateinit var loadStatus: IUiLoadStatus

    override fun initView() {
        loadStatus.initUiStatus(this,viewModel.statusView)
        binding?.login?.setOnClickListener {
            viewModel.login(binding?.login?.text.toString(), binding?.pwd?.text.toString())
        }
        viewModel.loginSuccess.observe(this) {
            Handler(Looper.getMainLooper()).postDelayed({
                navigationActivity(MainActivity::class.java)
                finish()
            },2000)
//            finish()
        }
    }
}