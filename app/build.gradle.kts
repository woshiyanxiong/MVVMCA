plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
}

apply(from = "${project.rootDir}/lib_base_package/router.gradle.kts")

android {
    compileSdk = 35
    namespace = "com.mvvm.demo"

    defaultConfig {
        applicationId = "com.mvvm.demo"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/gradle/incremental.annotation.processors",
                "META-INF/library_release.kotlin_module",
                "META-INF/*.kotlin_module",
                "io/ktor/network/sockets/SocketTimeoutException.kotlin_metadata",
                "META-INF/*.SF",
                "META-INF/*.DSA",
                "META-INF/*.RSA",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
                "META-INF/versions/9/module-info.class",
                "META-INF/FastDoubleParser-LICENSE",
                "META-INF/FastDoubleParser-NOTICE",
                "META-INF/DISCLAIMER",
                "META-INF/io.netty.versions.properties",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            )
        }
    }
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
    javacOptions {
        option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
    }
}

val hiltVersion = "2.56.2"

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.glide)
    implementation(libs.material)
    
    implementation(project(":core:common"))
    implementation(project(":data:mine"))
    implementation(project(":data:wallet"))
    implementation(project(":data:home"))
    implementation(project(":data:protocol"))
    implementation(project(":basecomponent:logcat"))
    implementation(project(":basecomponent:net"))
    implementation(project(":data:storage"))
    implementation(project(":feature:home"))
    implementation(project(":feature:home:api"))
    implementation(project(":feature:mine"))
    implementation(project(":feature:compose"))
    implementation(project(":router_annotation"))
    implementation(project(":router_compiler"))
    implementation(project(":lib_base_package"))
    
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")
}
