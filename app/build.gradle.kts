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
        versionCode = 2
        versionName = "1.0"
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-ads:24.6.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
}
