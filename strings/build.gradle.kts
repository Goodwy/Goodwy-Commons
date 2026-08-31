plugins {
    alias(libs.plugins.library)
    `maven-publish`
}

android {
    namespace = "com.goodwy.strings"
    compileSdk = libs.versions.app.build.compileSDKVersion.get().toInt()

    publishing {
        singleVariant("release") {}
    }
}

publishing.publications {
    create<MavenPublication>("release") {
        afterEvaluate {
            from(components["release"])
        }
    }
}
