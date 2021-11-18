package com.mvvm.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mvvm.home.ui.HomeFragment
import com.mvvm.logcat.LogUtils
import com.mvvm.mine.ui.MineFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch{
            repository.login("yanxiong","123456")
            supportFragmentManager.beginTransaction().add(R.id.fragment, MineFragment()).commitAllowingStateLoss()
        }
        LogUtils.d("ffff","fffffffffff")
        LogUtils.logAll()
    }

}