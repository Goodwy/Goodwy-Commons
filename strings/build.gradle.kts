plugins {
    alias(libs.plugins.library)
}

android {
    namespace = "com.goodwy.commons.strings"
    compileSdk = libs.versions.app.build.compileSDKVersion.get().toInt()
}
