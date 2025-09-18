plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:models"))
    implementation(project(":data:api"))

    // Supabase using version catalog
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.core)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    
    // Ktor client for Android
    implementation(libs.ktor.client.android)
    
    // JSON serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}