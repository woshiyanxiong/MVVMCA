package com.mvvm.demo

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mvvm.home.ui.HomeFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch{
            val data= withContext(Dispatchers.IO){
                repository.login("yanxiong","123456")
            }
            supportFragmentManager.beginTransaction().add(R.id.fragment,HomeFragment()).commitAllowingStateLoss()
        }

    }

}