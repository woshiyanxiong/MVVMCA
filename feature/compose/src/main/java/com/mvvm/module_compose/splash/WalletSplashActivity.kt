package com.mvvm.module_compose.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.mvvm.module_compose.ComPoseActivity
import com.mvvm.module_compose.WalletMainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
@Route(path = "/compose/splash")
class WalletSplashActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全屏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                
                SplashScreen(state = state)
                
                // 延迟3秒后检查密码
                LaunchedEffect(Unit) {
                    delay(3000)
                    viewModel.checkPassword()
                }
                
                // 监听导航状态
                LaunchedEffect(state.shouldNavigate) {
                    if (state.shouldNavigate) {
                        if (state.hasPassword) {
                            // 存在密码，跳转到登录页面
//                            ARouter.getInstance()
//                                .build("/wallet/login")
//                                .navigation()
                            startActivity(Intent(this@WalletSplashActivity, WalletMainActivity::class.java))


                        } else {
                            // 不存在密码，跳转到钱包初始化页面
                            startActivity(Intent(this@WalletSplashActivity, ComPoseActivity::class.java))
                        }
                        finish()
                    }
                }
            }
        }
    }
}