package com.mvvm.demo


import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.component.ext.navigationActivity
import com.mvvm.storage.LoginStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by yan_x
 * @date 2021/11/17/017 16:14
 * @description
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var userCase:LoginStore
    private var userToken:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        init()
        window.decorView.background = ColorDrawable(Color.parseColor("#ffffff"))
        lifecycleScope.launch {
            delay(2000)
            if (isLogin()){
                navigationActivity(MainActivity::class.java)
            }else{
                navigationActivity(LoginActivity::class.java)
            }

            this@SplashActivity.finish()
        }
    }

    private fun init(){
        lifecycleScope.launch {
            val data=userCase.getLoginToken().stateIn(lifecycleScope)
            withContext(Dispatchers.Main){
                data.value?.let {
                    userToken=it
                }
            }

        }
    }

    private fun isLogin():Boolean{
        return userToken.isNotBlank()
    }
}