pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "2.0.20-1.0.24"
        kotlin("jvm") version "2.0.20"
        // alias(libs.plugins.jetbrains.kotlin.jvm)
    }
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KotlionFBP_POC"
include(":app")
include(":graphLib")
include(":main-project")
