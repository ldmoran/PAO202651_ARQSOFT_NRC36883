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

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("junit:junit:4.13.2")
    // Servidor HTTP falso para las pruebas: com.sun.net.httpserver no existe en las pruebas de Android
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}
