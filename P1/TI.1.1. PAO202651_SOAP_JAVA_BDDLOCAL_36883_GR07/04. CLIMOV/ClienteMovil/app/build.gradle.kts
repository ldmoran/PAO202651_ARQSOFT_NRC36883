plugins {
    id("com.android.application")
}

android {
    namespace = "ec.edu.monster.clientemovil"
    compileSdk = 34

    defaultConfig {
        applicationId = "ec.edu.monster.clientemovil"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Sin librerias externas: solo clases estandar de Android y Java.
dependencies {
    testImplementation("junit:junit:4.13.2")
}
