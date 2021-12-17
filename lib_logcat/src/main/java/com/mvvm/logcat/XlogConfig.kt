package com.mvvm.logcat

import android.content.Context
import android.util.Log
import com.elvishew.xlog.XLog

import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy

import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy

import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator

import com.elvishew.xlog.printer.file.FilePrinter

import com.elvishew.xlog.printer.ConsolePrinter

import com.elvishew.xlog.printer.AndroidPrinter

import com.elvishew.xlog.interceptor.BlacklistTagsFilterInterceptor

import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.formatter.message.json.DefaultJsonFormatter
import com.elvishew.xlog.printer.Printer
import java.io.File


/**
 * Created by yan_x
 * @date 2021/12/17/017 17:05
 * @description
 */
object XlogConfig {
    private var logPath = ""
    fun init(context:Context) {
        logPath = context.getExternalFilesDir(null)
            ?.absolutePath + File.separator.toString() + "xlog"
        Log.e("日志地址", logPath)
        val config = LogConfiguration.Builder()
            .logLevel(
                if (BuildConfig.DEBUG) LogLevel.VERBOSE // 指定日志级别，低于该级别的日志将不会被打印，默认为 LogLevel.ALL
                else LogLevel.NONE
            )
            .tag("MY_TAG") // 指定 TAG，默认为 "X-LOG"
//            .enableThreadInfo() // 允许打印线程信息，默认禁止
//            .enableStackTrace(2) // 允许打印深度为 2 的调用栈信息，默认禁止
//            .enableBorder() // 允许打印日志边框，默认禁止
//            .jsonFormatter(DefaultJsonFormatter()) // 指定 JSON 格式化器，默认为 DefaultJsonFormatter
//            .xmlFormatter(MyXmlFormatter()) // 指定 XML 格式化器，默认为 DefaultXmlFormatter
//            .throwableFormatter(MyThrowableFormatter()) // 指定可抛出异常格式化器，默认为 DefaultThrowableFormatter
//            .threadFormatter(MyThreadFormatter()) // 指定线程信息格式化器，默认为 DefaultThreadFormatter
//            .stackTraceFormatter(MyStackTraceFormatter()) // 指定调用栈信息格式化器，默认为 DefaultStackTraceFormatter
//            .borderFormatter(MyBoardFormatter()) // 指定边框格式化器，默认为 DefaultBorderFormatter
//            .addObjectFormatter(
//                AnyClass::class.java,  // 为指定类型添加对象格式化器
//                AnyClassObjectFormatter()
//            ) // 默认使用 Object.toString()
            .addInterceptor(
                BlacklistTagsFilterInterceptor( // 添加黑名单 TAG 过滤器
                    "blacklist1", "blacklist2", "blacklist3"
                )
            )
//            .addInterceptor(MyInterceptor()) // 添加一个日志拦截器
            .build()

        val androidPrinter: Printer = AndroidPrinter(true) // 通过 android.util.Log 打印日志的打印器

        val consolePrinter: Printer = ConsolePrinter() // 通过 System.out 打印日志到控制台的打印器

        val filePrinter: Printer = FilePrinter.Builder(logPath) // 指定保存日志文件的路径
            .fileNameGenerator(DateFileNameGenerator()) // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
            .backupStrategy(NeverBackupStrategy()) // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
            .cleanStrategy(FileLastModifiedCleanStrategy(3 * 24 * 60 * 60 * 1000)) // 指定日志文件清除策略，默认为 NeverCleanStrategy()
//            .flattener(MyFlattener()) // 指定日志平铺器，默认为 DefaultFlattener
//            .writer(MyWriter()) // 指定日志写入器，默认为 SimpleWriter
            .build()

        XLog.init( // 初始化 XLog
            config,  // 指定日志配置，如果不指定，会默认使用 new LogConfiguration.Builder().build()
            androidPrinter,  // 添加任意多的打印器。如果没有添加任何打印器，会默认使用 AndroidPrinter(Android)/ConsolePrinter(java)
            filePrinter
        )
    }
}