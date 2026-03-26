pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "skom"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.smolder.cloud/public/")
        maven("https://jitpack.io")
        maven { url = uri("https://jitpack.io") }
    }
}

