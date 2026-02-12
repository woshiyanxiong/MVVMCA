# Implementation Plan: Gradle Kotlin DSL Migration

## Overview

本实施计划将 Android MVVM Demo 项目的 Gradle 构建文件从 Groovy DSL 迁移到 Kotlin DSL。采用自底向上的迁移策略，确保每一步都可独立验证。

## Tasks

- [x] 1. 更新版本目录 (libs.versions.toml)
  - [x] 1.1 将 version.gradle 中的依赖版本整合到 gradle/libs.versions.toml
    - 添加 lifecycle、activity-ktx、fragment-ktx、datastore 等版本
    - 添加对应的 libraries 声明
    - 添加 hilt gradle plugin 到 plugins 部分
    - _Requirements: 5.1_
  
  - [x] 1.2 添加 Gradle 插件依赖到版本目录
    - 添加 android-gradle-plugin、kotlin-gradle-plugin、hilt-gradle-plugin
    - _Requirements: 5.1_

- [x] 2. 迁移 basecomponent 库模块
  - [x] 2.1 迁移 basecomponent/logcat/build.gradle 为 build.gradle.kts
    - 转换 plugins 块语法
    - 转换 android 块配置（compileSdk、namespace、defaultConfig 等）
    - 转换 buildTypes 使用 getByName 或 release {}
    - 转换 dependencies 使用括号语法
    - 删除原 build.gradle 文件
    - _Requirements: 4.1, 4.3, 4.4, 4.5, 6.1, 6.2, 6.3_
  
  - [x] 2.2 迁移 basecomponent/net/build.gradle 为 build.gradle.kts
    - 转换 plugins 块语法
    - 转换 android 块配置
    - 转换 javaCompileOptions 注解处理器参数
    - 转换 dependencies 使用括号语法
    - 删除原 build.gradle 文件
    - _Requirements: 4.2, 4.3, 4.4, 4.5, 6.1, 6.2, 6.3_

- [x] 3. Checkpoint - 验证库模块迁移
  - 执行 `./gradlew :basecomponent:logcat:assemble :basecomponent:net:assemble`
  - 确保编译成功，如有问题请告知

- [x] 4. 迁移共享构建脚本
  - [x] 4.1 迁移 feature.gradle 为 feature.gradle.kts
    - 转换 apply plugin 语句
    - 转换 android 块配置
    - 转换 kapt 配置
    - 转换 dependencies 块，将 rootProject.ext.dependencies 引用更新为版本目录引用或直接字符串
    - 删除原 feature.gradle 文件
    - _Requirements: 5.2, 5.3, 6.1, 6.2, 6.3_
  
  - [x] 4.2 更新引用 feature.gradle 的模块
    - 将 `apply from: '../feature.gradle'` 更新为 `apply(from = "../feature.gradle.kts")`
    - _Requirements: 5.4_

- [x] 5. 迁移根目录构建文件
  - [x] 5.1 迁移 build.gradle 为 build.gradle.kts
    - 转换 buildscript 块，使用版本目录引用
    - 转换 repositories 块，使用 uri() 函数
    - 转换 dependencies 块，使用括号语法
    - 转换 subprojects 块，使用 Kotlin 语法
    - 转换 task clean 为 tasks.register<Delete>
    - 移除 apply from: 'version.gradle'（已整合到版本目录）
    - 删除原 build.gradle 文件
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 6.1, 6.2, 6.3, 6.4_
  
  - [x] 5.2 删除 version.gradle 文件
    - 确认所有依赖已迁移到版本目录
    - _Requirements: 5.4_

- [x] 6. Checkpoint - 验证根构建文件迁移
  - 执行 `./gradlew clean`
  - 确保任务执行成功，如有问题请告知

- [x] 7. 迁移 settings.gradle
  - [x] 7.1 迁移 settings.gradle 为 settings.gradle.kts
    - 转换 pluginManagement 块
    - 转换 dependencyResolutionManagement 块
    - 转换所有自定义函数 (includeWithApi, deleteEmptyDir, deleteDir, renameApiFiles, fileReader) 为 Kotlin 函数
    - 转换 include 语句使用括号和双引号
    - 删除原 settings.gradle 文件
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 6.1, 6.2, 6.4_

- [x] 8. Checkpoint - 验证 settings 迁移
  - 执行 `./gradlew projects`
  - 确保所有模块正确识别，如有问题请告知

- [x] 9. 迁移 app 模块
  - [x] 9.1 迁移 app/build.gradle 为 build.gradle.kts
    - 转换 plugins 块
    - 转换 apply from 语句
    - 转换 android 块配置（namespace、defaultConfig、buildFeatures、buildTypes、compileOptions、kotlinOptions、packaging）
    - 转换 kapt 配置
    - 转换 dependencies 块，使用版本目录引用和 project() 语法
    - 删除原 build.gradle 文件
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 6.1, 6.2, 6.3_

- [x] 10. 迁移其他模块的 build.gradle 文件
  - [x] 10.1 迁移 lib_base_package/build.gradle 为 build.gradle.kts
    - _Requirements: 4.3, 4.4, 4.5_
  
  - [x] 10.2 迁移 core/common/build.gradle 为 build.gradle.kts
    - _Requirements: 4.3, 4.4, 4.5_
  
  - [x] 10.3 迁移 core/resource/build.gradle 为 build.gradle.kts
    - _Requirements: 4.3, 4.4, 4.5_
  
  - [x] 10.4 迁移 data 目录下所有模块的 build.gradle 为 build.gradle.kts
    - 包括 storage、mine、protocol、home、wallet
    - _Requirements: 4.3, 4.4, 4.5_
  
  - [x] 10.5 迁移 feature 目录下所有模块的 build.gradle 为 build.gradle.kts
    - 包括 home、mine、compose
    - _Requirements: 4.3, 4.4, 4.5_
  
  - [x] 10.6 迁移 router 相关模块的 build.gradle 为 build.gradle.kts
    - 包括 router_annotation、router_compiler、router_processor、router_gradle_plugin
    - _Requirements: 4.3, 4.4, 4.5_

- [x] 11. Final Checkpoint - 完整构建验证
  - 执行 `./gradlew clean assembleDebug`
  - 确保完整构建成功
  - 验证 Hilt 依赖注入正常工作
  - 验证 ARouter 注解处理正常工作
  - 验证 DataBinding 正常工作
  - _Requirements: 7.1, 7.2, 7.3, 8.1, 8.2, 8.3, 8.4, 8.5_

## Notes

- 每个 Checkpoint 任务用于验证阶段性迁移结果，如遇到问题应及时修复
- 迁移过程中保留原 .gradle 文件作为参考，确认 .kts 文件工作正常后再删除
- 如果某个模块迁移失败，可以暂时保留 .gradle 文件，继续迁移其他模块
- 版本目录 (libs.versions.toml) 是推荐的依赖管理方式，但部分依赖可能需要保持字符串形式
