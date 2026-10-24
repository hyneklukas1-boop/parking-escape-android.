plugins {
    id("com.android.application")
}

android {
    buildFeatures {
        buildConfig = true
    }

    namespace = "cz.lakys18.parkingescape"
    compileSdk = 36

    defaultConfig {
        applicationId = "cz.lakys18.parkingescape"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.1"
    }
}

dependencies {
    // Replace the obsolete transitive Fragment 1.1.0 flagged by Play Console.
    implementation("androidx.fragment:fragment:1.8.9")
    implementation("com.google.android.gms:play-services-ads:24.9.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
}

// Resolve both variants: the Play warning must be fixed in the shipped graph.
tasks.register("verifyFragmentDependency") {
    doLast {
        listOf("debugRuntimeClasspath", "releaseRuntimeClasspath").forEach { name ->
            val fragment = configurations.getByName(name).resolvedConfiguration.resolvedArtifacts
                .single { it.moduleVersion.id.group == "androidx.fragment" && it.name == "fragment" }
            check(fragment.moduleVersion.id.version == "1.8.9") {
                "Unexpected Fragment dependency in $name: ${fragment.moduleVersion.id}"
            }
            println("$name: ${fragment.moduleVersion.id}")
        }
    }
}
