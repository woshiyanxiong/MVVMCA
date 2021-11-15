package com.mvvm.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mvvm.home.ui.HomeFragment
import com.mvvm.logcat.LogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject





/**
 * Created by yan_x
 * @date 2021/11/5/005 15:59
 * @description
 */
@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    @Inject
    lateinit var repository: UserRepository

    private val testString:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch{
            val data= withContext(Dispatchers.IO){
                repository.login("yanxiong","123456")
            }
            supportFragmentManager.beginTransaction().add(R.id.fragment,HomeFragment()).commitAllowingStateLoss()
        }
//        Log.e("testString",testString!!)
        LogUtils.d("ffff","fffffffffff")
        LogUtils.logAll()

    }

}