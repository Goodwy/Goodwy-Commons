plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.parcelize)
    `maven-publish`
}

group = "com.goodwy"
version = "5.2.0"

android {
    namespace = "com.goodwy.strings"
    compileSdk = libs.versions.app.build.compileSDKVersion.get().toInt()
}

publishing.publications {
    create<MavenPublication>("release") {
        afterEvaluate {
            from(components["release"])
        }
    }
}
