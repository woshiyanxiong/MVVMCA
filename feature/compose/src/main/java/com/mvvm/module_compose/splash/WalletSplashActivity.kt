package com.mvvm.module_compose.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme
import com.alibaba.android.arouter.facade.annotation.Route
import com.mvvm.module_compose.CreateWalletActivity
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
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                
                SplashScreen(state = state)
                
                // 延迟3秒后检查钱包
                LaunchedEffect(Unit) {
                    delay(3000)
                    viewModel.checkWallet()
                }
                
                // 监听导航状态
                LaunchedEffect(state.shouldNavigate) {
                    if (state.shouldNavigate) {
                        if (state.hasWallet) {
                            startActivity(Intent(this@WalletSplashActivity, WalletMainActivity::class.java))
                        } else {
                            startActivity(Intent(this@WalletSplashActivity, CreateWalletActivity::class.java))
                        }
                        finish()
                    }
                }
            }
        }
    }
}