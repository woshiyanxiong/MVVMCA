package com.mvvm.logcat.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mvvm.logcat.FloatingLogcatService
import com.mvvm.logcat.R

/**
 * Created by yan_x
 * @date 2021/12/17/017 14:40
 * @description 调试界面
 */
class LogcatToolActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logcat_tool)
        findViewById<ImageView>(R.id.ivBlack).setOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.tvStartLog).setOnClickListener {
            startService(Intent(this, FloatingLogcatService::class.java))
        }
    }
}