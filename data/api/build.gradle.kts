plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Expose models to downstream modules (interfaces use these types)
    api(project(":core:models"))

    // Coroutines (if your interfaces use Flow/Coroutine types)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
