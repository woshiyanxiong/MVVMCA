import java.io.File

pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("./router_gradle_plugin")
            name = "MavenRepo"
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
        mavenLocal()
        google()
        mavenCentral()
        maven {
            url = uri("./router_gradle_plugin")
        }
    }
}

fun includeWithApi(moduleName: String, parentFileName: String) {
    // 先正常加载这个模块
    include(moduleName)
    // 找到这个模块的路径
    val originDir = project(moduleName).projectDir.absolutePath
    // 这个是新的路径
    val targetDir = "${originDir}_api"
    // 原模块的名字
    val originName = project(moduleName).name
    // 新模块的名字
    val sdkName = "${originName}_api"

    // todo 替换成自己的公共模块，或者预先放api.gradle的模块
    // 这个是公共模块的位置，我预先放了一个 新建的api.gradle 文件进去
    val apiGradle = project(":lib_base_package").projectDir.absolutePath

    // 每次编译删除之前的文件
    deleteDir(targetDir)

    // 复制.api文件到新的路径
    copy {
        from(originDir)
        into(targetDir)
        exclude("**/build/")
        exclude("**/res/")
        include("**/*.api")
        include("**/*.kapi")
    }

    // 直接复制公共模块的AndroidManifest文件到新的路径，作为该模块的文件
    copy {
        from("$apiGradle/src/main/AndroidManifest.xml")
        into("$targetDir/src/main/")
    }

    // 复制 gradle文件到新的路径，作为该模块的gradle
    copy {
        from("$apiGradle/api.gradle")
        into(targetDir)
    }

    // 删除空文件夹
    deleteEmptyDir(File(targetDir))

    // 为AndroidManifest新建路径，路径就是在原来的包下面新建一个api包，作为AndroidManifest里面的包名
    val packagePath = "$targetDir/src/main/java/com/mvvm/$originName/api"

    // 修改AndroidManifest文件包路径
    fileReader("$targetDir/src/main/AndroidManifest.xml", "lib_base_package", "$originName.api")

    File(packagePath).mkdirs()

    // 重命名一下gradle
    val build = File("$targetDir/api.gradle")
    if (build.exists()) {
        build.renameTo(File("$targetDir/build.gradle"))
    }

    // 重命名.api文件，生成正常的.java文件
    renameApiFiles(targetDir, ".kapi", ".kt")

    val includeName = if (parentFileName.isEmpty()) ":$sdkName" else ":$parentFileName:$sdkName"
    println("加载的api文件：$sdkName，$targetDir,$originName,$includeName")
    // 正常加载新的模块
    include(includeName)
}

fun deleteEmptyDir(dir: File) {
    if (dir.isDirectory) {
        val fs = dir.listFiles()
        if (fs != null && fs.isNotEmpty()) {
            for (tmpFile in fs) {
                if (tmpFile.isDirectory) {
                    deleteEmptyDir(tmpFile)
                }
                if (tmpFile.isDirectory && (tmpFile.listFiles()?.isEmpty() == true)) {
                    tmpFile.delete()
                }
            }
        }
        if (dir.isDirectory && (dir.listFiles()?.isEmpty() == true)) {
            dir.delete()
        }
    }
}

fun deleteDir(targetDir: String) {
    val targetFiles = fileTree(targetDir)
    targetFiles.exclude("*.iml")
    targetFiles.forEach { file ->
        file.delete()
    }
}

fun renameApiFiles(rootDir: String, suffix: String, replace: String) {
    val files = fileTree(rootDir).matching { include("**/*$suffix") }
    files.forEach { file ->
        file.renameTo(File(file.absolutePath.replace(suffix, replace)))
    }
    val filesApi = fileTree(rootDir).matching { include("**/*.api") }
    filesApi.forEach { file ->
        file.renameTo(File(file.absolutePath.replace(".api", ".java")))
    }
}

fun fileReader(path: String, name: String, sdkName: String) {
    val file = file(path)
    if (!file.exists()) return
    
    var readerString = StringBuilder()
    var hasReplace = false

    file.bufferedReader(Charsets.UTF_8).useLines { lines ->
        lines.forEach { line ->
            var modifiedLine = line
            if (line.contains(name)) {
                modifiedLine = line.replace(name, sdkName)
                hasReplace = true
            }
            readerString.append(modifiedLine)
            readerString.append("\n")
        }
    }

    if (hasReplace) {
        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(readerString.toString())
        }
    }
}

rootProject.name = "CA_demo"
include(":app")
include(":lib_base_package")
include(":core:common")
include(":core:resource")
include(":basecomponent:logcat")
include(":basecomponent:net")
include(":data:storage")
include(":data:mine")
include(":feature:home")
include(":feature:mine")
include(":feature:compose")
include(":data:protocol")
include(":data:home")
include(":router_annotation")
include(":router_processor")
include(":router_compiler")
include(":feature:home:api")
include(":router_gradle_plugin")
include(":data:wallet")
// include(":domain:wallet")
// include(":data:data_template")
