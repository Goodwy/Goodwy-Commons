pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://developer.huawei.com/repo/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://developer.huawei.com/repo/") }
        maven { setUrl("https://artifactory-external.vkpartner.ru/artifactory/maven") }
    }
}
rootProject.name = "Goodwy-Commons"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":commons", ":samples", ":strings")
