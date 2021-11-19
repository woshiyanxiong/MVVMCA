package com.mvvm.demo


import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.common.ext.navigationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by yan_x
 * @date 2021/11/17/017 16:14
 * @description
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.decorView.background = ColorDrawable(Color.parseColor("#ffffff"))
        lifecycleScope.launch {
            delay(2000)
            navigationActivity(MainActivity::class.java)
            this@SplashActivity.finish()
        }
    }
}