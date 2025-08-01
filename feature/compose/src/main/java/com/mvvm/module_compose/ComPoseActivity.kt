package com.mvvm.module_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import com.alibaba.android.arouter.facade.annotation.Route

/**
 * Created by yan_x
 * @date 2022/6/29/029 14:05
 * @description
 */
@Route(path = "/feature/compose/main")
class ComPoseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
           Text(text = "compose")
        }
    }
}