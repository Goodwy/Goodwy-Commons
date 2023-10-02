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
        maven { setUrl("https://jitpack.io") }
        maven { url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven") }
    }
}
rootProject.name = "Goodwy-Commons"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":samples", "commons")
