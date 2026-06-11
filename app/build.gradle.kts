// Build.gradle.kts (Module :app)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // <--- AIXÒ HA D'ESTAR AQUÍ
}

android {
    namespace = "com.guillem.repeticiollistacompra"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.guillem.repeticiollistacompra"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Llibreries base d'Android (utilitzant el catàleg libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- DEPENDÈNCIES DE FIREBASE (Netejat i centralitzat) ---
    // Importa el BOM des del catàleg
    implementation(platform(libs.firebase.bom))

    // Llibreries de Firebase (ara sense necessitat d'especificar la versió aquí)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}