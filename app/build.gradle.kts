plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.auag0.disableaudiofocus"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.auag0.disableaudiofocus"
        minSdk = 24
        targetSdk = 35
        versionCode = 101000
        versionName = "1.1.0"
    }

    signingConfigs {
        create("release") {
            val storePass = System.getenv("storePassword")
            if (!storePass.isNullOrEmpty()) {
                storeFile = File(projectDir, "release-keystore.jks")
                storePassword = storePass
                keyAlias = System.getenv("keyAlias")
                keyPassword = System.getenv("keyPassword")
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources.excludes.add("**/kotlin/**")
        resources.excludes.add("kotlin-tooling-metadata.json")
    }
}

dependencies {
    compileOnly(libs.libxposed.api)
}