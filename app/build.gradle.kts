import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun secret(name: String): String {
    val fromFile = localProperties.getProperty(name)?.trim().orEmpty()
    if (fromFile.isNotEmpty()) return fromFile
    return System.getenv(name)?.trim().orEmpty()
}

val releaseAppId = secret("ADMOB_APP_ID")
val releaseBannerId = secret("ADMOB_BANNER_AD_UNIT_ID")
val releaseAdsReady = releaseAppId.isNotEmpty() && releaseBannerId.isNotEmpty()

val releaseManifestFile = rootProject.file("app/generated-release-manifest/AndroidManifest.xml")
releaseManifestFile.parentFile.mkdirs()
releaseManifestFile.writeText(
    if (releaseAdsReady) {
        """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
            <application>
                <meta-data
                    android:name="com.google.android.gms.ads.APPLICATION_ID"
                    android:value="${releaseAppId}" />
            </application>
        </manifest>
        """.trimIndent()
    } else {
        """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools">
            <application>
                <!-- Sin App ID real no se deja arrancar el proveedor de anuncios. -->
                <provider
                    android:name="com.google.android.gms.ads.MobileAdsInitProvider"
                    tools:node="remove" />
            </application>
        </manifest>
        """.trimIndent()
    },
)

val storeFilePath = secret("RELEASE_STORE_FILE")
val releaseStorePassword = secret("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = secret("RELEASE_KEY_ALIAS")
val releaseKeyPassword = secret("RELEASE_KEY_PASSWORD")
val releaseSigningReady = storeFilePath.isNotEmpty() && releaseStorePassword.isNotEmpty() &&
    releaseKeyAlias.isNotEmpty() && releaseKeyPassword.isNotEmpty()

base {
    archivesName.set("Guia-Heartopia-0.4.0")
}

android {
    namespace = "com.guiaheartopia.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.guiaheartopia.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 4
        versionName = "0.4.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningReady) {
            create("release") {
                storeFile = rootProject.file(storeFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // ID oficiales de prueba de Google. Solo esta variante.
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField("boolean", "ADS_USE_GOOGLE_TEST_IDS", "true")
            val geo = secret("UMP_DEBUG_GEOGRAPHY")
            val device = secret("UMP_TEST_DEVICE_ID")
            buildConfigField("String", "UMP_DEBUG_GEOGRAPHY", "\"${geo.replace("\"", "")}\"")
            buildConfigField("String", "UMP_TEST_DEVICE_ID", "\"${device.replace("\"", "")}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "ADMOB_APP_ID", "\"${releaseAppId.replace("\"", "")}\"")
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"${releaseBannerId.replace("\"", "")}\"")
            buildConfigField("boolean", "ADS_ENABLED", releaseAdsReady.toString())
            buildConfigField("boolean", "ADS_USE_GOOGLE_TEST_IDS", "false")
            buildConfigField("String", "UMP_DEBUG_GEOGRAPHY", "\"\"")
            buildConfigField("String", "UMP_TEST_DEVICE_ID", "\"\"")
            if (releaseSigningReady) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("release") {
            manifest.srcFile(releaseManifestFile)
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    val bom = platform(libs.compose.bom)
    implementation(bom)
    androidTestImplementation(bom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.core.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.work.runtime)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
