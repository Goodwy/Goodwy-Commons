plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlinAndroid)
    `maven-publish`
}

group = "com.goodwy"
version = "5.5.0"

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
