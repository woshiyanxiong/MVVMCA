plugins { id("data-base") }

android {
    namespace = "com.yupao.feature.ui_template"
}

dependencies {
    implementation(project(":feature_block:cms"))
    implementation(project(":data:cms"))
    implementation(project(":model:cms"))

}