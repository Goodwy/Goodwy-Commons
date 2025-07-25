import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.detekt)
    `maven-publish`
}

group = "com.goodwy"
version = "6.3.1"

android {
    namespace = "com.goodwy.commons"

    compileSdk = libs.versions.app.build.compileSDKVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.app.build.minimumSDK.get().toInt()
        vectorDrawables.useSupportLibrary = true
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    flavorDimensions.add("variants")
    productFlavors {
        register("core") //Google Play
        register("foss")
        register("rustore")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {}
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        val currentJavaVersionFromLibs =
            JavaVersion.valueOf(libs.versions.app.build.javaVersion.get())
        sourceCompatibility = currentJavaVersionFromLibs
        targetCompatibility = currentJavaVersionFromLibs
    }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(
            JvmTarget.fromTarget(project.libs.versions.app.build.kotlinJVMTarget.get())
        )
        compilerOptions.freeCompilerArgs.set(
            listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi",
                "-Xcontext-receivers"
            )
        )
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = true
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")
        lintConfig = rootProject.file("lint.xml")
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

publishing.publications {
    create<MavenPublication>("coreRelease") {
        artifactId = "core"
        afterEvaluate {
            from(components["coreRelease"])
        }
    }
    create<MavenPublication>("fossRelease") {
        artifactId = "foss"
        afterEvaluate {
            from(components["fossRelease"])
        }
    }
    create<MavenPublication>("rustoreRelease") {
        artifactId = "rustore"
        afterEvaluate {
            from(components["rustoreRelease"])
        }
    }
}

detekt {
    baseline = file("detekt-baseline.xml")
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(libs.kotlin.immutable.collections)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.ez.vcard)

    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.compose)
    implementation(libs.compose.view.binding)
    debugImplementation(libs.bundles.compose.preview)

    api(libs.joda.time)
    api(libs.recyclerView.fastScroller)
    api(libs.reprint)
    api(libs.rtl.viewpager)
    api(libs.patternLockView)
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.gson)

    implementation(libs.glide.compose)
    api(libs.glide)
    ksp(libs.glide.compiler)

    api(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    detektPlugins(libs.compose.detekt)

    //Goodwy
    api(projects.strings)
    "coreApi"(libs.billing.client)
    "rustoreApi"(libs.rustore.client)
    implementation(libs.behavio.rule)
    implementation(libs.rx.animation)
    implementation(libs.rx.java)
}
