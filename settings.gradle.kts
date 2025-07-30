pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    val isRuStoreBuild = gradle.startParameter.taskRequests
        .any { it.args.any { arg -> arg.contains("rustore", ignoreCase = true) } }

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }

        if (isRuStoreBuild) {
            maven { setUrl("https://artifactory-external.vkpartner.ru/artifactory/maven") }
        }
    }
}
rootProject.name = "Goodwy-Commons"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":commons", ":samples", ":strings")
