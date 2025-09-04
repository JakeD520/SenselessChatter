plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // If you want strictly zero dependencies, leave empty.
    // Add stdlib explicitly for clarity (Gradle usually brings it in).
    implementation(kotlin("stdlib"))
}
