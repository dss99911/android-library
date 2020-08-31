buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("com.google.gms:google-services:4.3.3")
    }
}

plugins {
    id("com.android.application")
    id("kotlin-android")
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "kim.jeonghyeon.kotlin-simple-architecture-gradle-plugin")

val androidKeyAlias: String by project
val androidKeyPassword: String by project
val androidStoreFile: String by project
val androidStorePassword: String by project

android {

    val appId = "kim.jeonghyeon.sample.compose"

    compileSdkVersion(config.compileSdkVersion)
    buildToolsVersion(config.buildToolVersion)
    defaultConfig {
        versionCode = 10002
        versionName = "1.00.02"
        minSdkVersion(config.minSdkVersion)
        targetSdkVersion(config.targetSdkVersion)

        buildConfigField("String", "freePackageName", "\"${appId}\"")

        buildConfigField("boolean", "isFree", "false")
        buildConfigField("boolean", "isPro", "false")
        buildConfigField("boolean", "isDev", "false")
        buildConfigField("boolean", "isProd", "false")
        buildConfigField("boolean", "isMock", "false")
    }

    flavorDimensions("mode", "stage")

    val FLAVOR_NAME_MOCK = "mock"

    productFlavors {
        val free by creating {
            dimension = "mode"
            applicationId = appId
            buildConfigField("boolean", "isFree", "true")
        }

        val pro by creating {
            dimension = "mode"
            applicationId = appId + ".pro"
            buildConfigField("boolean", "isPro", "true")
        }

        val dev by creating {
            dimension = "stage"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField("boolean", "isDev", "true")
            //optimize build time
            resConfigs("en", "hdpi")
            minSdkVersion(if (config.minSdkVersion > 21) config.minSdkVersion else 21)
        }

        val prod by creating {
            dimension = "stage"
            buildConfigField("boolean", "isProd", "true")
        }

        create(FLAVOR_NAME_MOCK) {
            dimension = "stage"

            applicationIdSuffix = ".mock"
            versionNameSuffix = "-mock"

            buildConfigField("boolean", "isMock", "true")
            //optimize build time
            resConfigs("en", "hdpi")
            minSdkVersion(if (config.minSdkVersion > 21) config.minSdkVersion else 21)
        }
    }

    val SIGNING_CONFIG_NAME_RELEASE = "release"

    signingConfigs {
        create(SIGNING_CONFIG_NAME_RELEASE) {
            keyAlias = androidKeyAlias
            keyPassword = androidKeyPassword
            storeFile = file(androidStoreFile)
            storePassword = androidStorePassword
        }
    }

    val BUILD_TYPE_NAME_DEBUG = "debug"
    val BUILD_TYPE_NAME_RELEASE = "release"

    buildTypes {
        getByName(BUILD_TYPE_NAME_DEBUG) {
            isTestCoverageEnabled = true
        }

        getByName(BUILD_TYPE_NAME_RELEASE) {
            isMinifyEnabled = true
            isShrinkResources = true
            isZipAlignEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName(SIGNING_CONFIG_NAME_RELEASE)
        }
    }

    // Always show the result of every unit test, even if it passes.
    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }


    //region Jetpack Compose todo try to move to library?

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerVersion = versions.kotlin.version
        kotlinCompilerExtensionVersion = "1.0.0-alpha01"
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }

    //endregion Jetpack Compose

}

dependencies {
    implementation(project(":sample:sample-base"))

    implementation("com.google.firebase:firebase-analytics:17.2.2")
    implementation("com.google.firebase:firebase-dynamic-links-ktx:19.1.0")
}