pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rivvr"

// Make sure every module you have is included here:
include(
    ":app",
    ":core:models",
    ":data:api",
    ":data:impl-supabase",
    ":data:impl-stub"
)
