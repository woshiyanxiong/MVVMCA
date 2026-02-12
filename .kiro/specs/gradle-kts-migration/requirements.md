# Requirements Document

## Introduction

本文档定义了将 Android MVVM Demo 项目的 Gradle 构建文件从 Groovy DSL 迁移到 Kotlin DSL (.kts) 格式的需求。项目包含多个模块，需要系统性地迁移所有 Gradle 配置文件，同时保持构建功能的完整性和正确性。

## Glossary

- **Gradle_Migration_System**: 负责执行 Gradle 文件迁移的系统
- **Groovy_DSL**: Gradle 的 Groovy 领域特定语言格式 (.gradle)
- **Kotlin_DSL**: Gradle 的 Kotlin 领域特定语言格式 (.gradle.kts)
- **Build_Configuration**: 包含编译选项、依赖声明、插件配置的构建设置
- **Version_Catalog**: Gradle 版本目录 (libs.versions.toml)，用于集中管理依赖版本
- **Root_Build_File**: 根目录的 build.gradle 文件
- **Module_Build_File**: 各模块目录下的 build.gradle 文件
- **Settings_File**: settings.gradle 文件，定义项目结构和模块包含关系
- **Shared_Build_Script**: 共享的构建脚本文件 (如 version.gradle, feature.gradle)

## Requirements

### Requirement 1: 根目录构建文件迁移

**User Story:** 作为开发者，我希望将根目录的 build.gradle 迁移为 build.gradle.kts，以便使用 Kotlin 的类型安全和 IDE 支持。

#### Acceptance Criteria

1. WHEN 迁移根目录 build.gradle THEN THE Gradle_Migration_System SHALL 将文件重命名为 build.gradle.kts 并转换所有 Groovy 语法为 Kotlin 语法
2. WHEN 转换 buildscript 块 THEN THE Gradle_Migration_System SHALL 将 ext 属性声明转换为 Kotlin 的 extra 属性或 buildSrc/版本目录方式
3. WHEN 转换 repositories 块 THEN THE Gradle_Migration_System SHALL 将 maven { url = "..." } 转换为 maven { url = uri("...") } 或 maven("...")
4. WHEN 转换 dependencies 块 THEN THE Gradle_Migration_System SHALL 将字符串插值 "$variable" 转换为 Kotlin 字符串模板 "${variable}"
5. WHEN 转换 subprojects 块 THEN THE Gradle_Migration_System SHALL 保持 afterEvaluate 逻辑并使用 Kotlin 语法
6. WHEN 转换 task 声明 THEN THE Gradle_Migration_System SHALL 将 task clean(type: Delete) 转换为 tasks.register<Delete>("clean")

### Requirement 2: Settings 文件迁移

**User Story:** 作为开发者，我希望将 settings.gradle 迁移为 settings.gradle.kts，以便项目设置也能享受 Kotlin DSL 的优势。

#### Acceptance Criteria

1. WHEN 迁移 settings.gradle THEN THE Gradle_Migration_System SHALL 将文件重命名为 settings.gradle.kts 并转换所有语法
2. WHEN 转换 pluginManagement 块 THEN THE Gradle_Migration_System SHALL 保持仓库配置并使用 Kotlin 语法
3. WHEN 转换 dependencyResolutionManagement 块 THEN THE Gradle_Migration_System SHALL 使用 RepositoriesMode.PREFER_SETTINGS 枚举
4. WHEN 转换自定义函数 (如 includeWithApi) THEN THE Gradle_Migration_System SHALL 将 Groovy 函数转换为 Kotlin 函数，包括参数类型声明
5. WHEN 转换文件操作 (copy, delete, fileTree) THEN THE Gradle_Migration_System SHALL 使用 Kotlin 等效的 Gradle API
6. WHEN 转换 include 语句 THEN THE Gradle_Migration_System SHALL 保持所有模块包含关系不变

### Requirement 3: App 模块构建文件迁移

**User Story:** 作为开发者，我希望将 app/build.gradle 迁移为 Kotlin DSL，以便主应用模块使用类型安全的构建配置。

#### Acceptance Criteria

1. WHEN 迁移 app/build.gradle THEN THE Gradle_Migration_System SHALL 将文件重命名为 build.gradle.kts
2. WHEN 转换 plugins 块 THEN THE Gradle_Migration_System SHALL 将 id 'plugin-id' 转换为 id("plugin-id")
3. WHEN 转换 apply from 语句 THEN THE Gradle_Migration_System SHALL 使用 apply(from = "path") 语法
4. WHEN 转换 android 块 THEN THE Gradle_Migration_System SHALL 使用 Kotlin 属性赋值语法 (= 而非空格)
5. WHEN 转换 dependencies 块 THEN THE Gradle_Migration_System SHALL 将 implementation project(':module') 转换为 implementation(project(":module"))
6. WHEN 转换 kapt 配置 THEN THE Gradle_Migration_System SHALL 保持注解处理器参数配置
7. WHEN 转换 packagingOptions THEN THE Gradle_Migration_System SHALL 使用 resources.excludes.add() 语法

### Requirement 4: Library 模块构建文件迁移

**User Story:** 作为开发者，我希望将所有 library 模块的 build.gradle 迁移为 Kotlin DSL，以保持项目构建配置的一致性。

#### Acceptance Criteria

1. WHEN 迁移 basecomponent/logcat/build.gradle THEN THE Gradle_Migration_System SHALL 转换为 build.gradle.kts 并保持所有配置
2. WHEN 迁移 basecomponent/net/build.gradle THEN THE Gradle_Migration_System SHALL 转换为 build.gradle.kts 并保持所有配置
3. WHEN 转换 compileSdkVersion/minSdkVersion THEN THE Gradle_Migration_System SHALL 使用 compileSdk = 值 语法
4. WHEN 转换 buildTypes 块 THEN THE Gradle_Migration_System SHALL 使用 getByName("release") 或 named("release") 访问构建类型
5. WHEN 转换 dependencies 块 THEN THE Gradle_Migration_System SHALL 使用括号语法 implementation("group:artifact:version")

### Requirement 5: 共享构建脚本迁移

**User Story:** 作为开发者，我希望将共享的构建脚本 (version.gradle, feature.gradle) 迁移为 Kotlin DSL 或整合到版本目录中。

#### Acceptance Criteria

1. WHEN 迁移 version.gradle THEN THE Gradle_Migration_System SHALL 将依赖版本定义整合到 gradle/libs.versions.toml 版本目录中
2. WHEN 迁移 feature.gradle THEN THE Gradle_Migration_System SHALL 转换为 feature.gradle.kts 或创建 convention plugin
3. WHEN 转换 ext 依赖映射 THEN THE Gradle_Migration_System SHALL 将 rootProject.ext.dependencies 引用更新为版本目录引用
4. IF version.gradle 被移除 THEN THE Gradle_Migration_System SHALL 更新所有引用该文件的 apply from 语句

### Requirement 6: 语法转换规则

**User Story:** 作为开发者，我希望所有 Groovy 到 Kotlin 的语法转换遵循一致的规则，以确保迁移的正确性。

#### Acceptance Criteria

1. THE Gradle_Migration_System SHALL 将所有单引号字符串 'string' 转换为双引号字符串 "string"
2. THE Gradle_Migration_System SHALL 将所有方法调用 method arg 转换为 method(arg) 括号语法
3. THE Gradle_Migration_System SHALL 将所有属性赋值 property value 转换为 property = value
4. THE Gradle_Migration_System SHALL 将 def 变量声明转换为 val 或 var
5. THE Gradle_Migration_System SHALL 将 Groovy 闭包 { } 转换为 Kotlin lambda 或 Action<T>
6. THE Gradle_Migration_System SHALL 将 == 字符串比较保持不变 (Kotlin 中 == 是结构相等)

### Requirement 7: 构建验证

**User Story:** 作为开发者，我希望迁移后的项目能够正常构建，以确保迁移没有破坏任何功能。

#### Acceptance Criteria

1. WHEN 迁移完成 THEN THE Gradle_Migration_System SHALL 确保 ./gradlew clean 命令成功执行
2. WHEN 迁移完成 THEN THE Gradle_Migration_System SHALL 确保 ./gradlew assembleDebug 命令成功执行
3. WHEN 迁移完成 THEN THE Gradle_Migration_System SHALL 确保所有模块的依赖解析正确
4. IF 构建失败 THEN THE Gradle_Migration_System SHALL 提供错误诊断和修复建议

### Requirement 8: 兼容性保持

**User Story:** 作为开发者，我希望迁移过程保持与现有工具和流程的兼容性。

#### Acceptance Criteria

1. THE Gradle_Migration_System SHALL 保持 Hilt 依赖注入配置的正确性
2. THE Gradle_Migration_System SHALL 保持 ARouter 注解处理器配置的正确性
3. THE Gradle_Migration_System SHALL 保持 DataBinding 配置的正确性
4. THE Gradle_Migration_System SHALL 保持自定义 router_gradle_plugin 的集成
5. THE Gradle_Migration_System SHALL 保持 includeWithApi 自定义模块加载逻辑的功能
