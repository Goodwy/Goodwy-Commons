pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //maven { url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven") }
        maven { setUrl("https://jitpack.io") }
    }
}
rootProject.name = "Goodwy-Commons"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":samples", "commons")
