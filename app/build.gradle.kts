import java.io.File
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

fun admobPlaceholder(id: String): Boolean {
    val value = id.trim()
    if (value.isEmpty()) return true
    val lower = value.lowercase()
    return lower.contains("xxxx") ||
        lower.contains("yyyy") ||
        lower.contains("placeholder") ||
        lower.contains("tu_id") ||
        lower.contains("reemplazar") ||
        lower.contains("ejemplo") ||
        lower.startsWith("admob_")
}

val testAdMobApp = "ca-app-pub-3940256099942544~3347511713"
val testAdMobBanner = "ca-app-pub-3940256099942544/6300978111"
val testAdMobInterstitial = "ca-app-pub-3940256099942544/1033173712"
val testAdMobRewarded = "ca-app-pub-3940256099942544/5224354917"

val releaseAppId = secret("ADMOB_APP_ID")
val releaseBannerId = secret("ADMOB_BANNER_AD_UNIT_ID")
val releaseInterstitialId = secret("ADMOB_INTERSTITIAL_AD_UNIT_ID")
val releaseRewardedCalendarId = secret("ADMOB_REWARDED_CALENDAR_AD_UNIT_ID")
val releaseRewardedAdFreeId = secret("ADMOB_REWARDED_ADFREE_AD_UNIT_ID")
val releaseAdsReady = releaseAppId.isNotEmpty() && releaseBannerId.isNotEmpty()
val releaseUseRealAdIds = !admobPlaceholder(releaseAppId) && !admobPlaceholder(releaseBannerId)
val releaseUseTestAdIds = !releaseUseRealAdIds
val releaseAdsEnabled = releaseUseTestAdIds || releaseAdsReady

fun effectiveAdId(real: String, test: String, useTest: Boolean): String =
    if (useTest) test else real.trim().takeUnless { admobPlaceholder(it) }.orEmpty()

val effectiveReleaseAppId = effectiveAdId(releaseAppId, testAdMobApp, releaseUseTestAdIds)

val releaseManifestFile = rootProject.file("app/generated-release-manifest/AndroidManifest.xml")
releaseManifestFile.parentFile.mkdirs()
releaseManifestFile.writeText(
    if (releaseAdsEnabled) {
        """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
            <application>
                <meta-data
                    android:name="com.google.android.gms.ads.APPLICATION_ID"
                    android:value="${effectiveReleaseAppId}" />
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

val distroKeystore = File(System.getProperty("user.home"), ".wikifan-heartopia/release.jks")
val distroSigningReady = distroKeystore.isFile

val uploadDir = File(System.getProperty("user.home"), ".wikifan-heartopia")
val uploadProps = Properties().apply {
    val file = File(uploadDir, "upload.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun uploadSecret(name: String): String = uploadProps.getProperty(name)?.trim().orEmpty()
val uploadKeystore = File(uploadDir, uploadSecret("storeFile").ifEmpty { "upload-key.jks" })
val uploadStorePassword = uploadSecret("storePassword")
val uploadKeyAlias = uploadSecret("keyAlias")
val uploadKeyPassword = uploadSecret("keyPassword")
val uploadSigningReady = uploadKeystore.isFile &&
    uploadStorePassword.isNotEmpty() &&
    uploadKeyAlias.isNotEmpty() &&
    uploadKeyPassword.isNotEmpty()

base {
    archivesName.set("Guia-Heartopia-1.0.8")
}

android {
    namespace = "com.guiaheartopia.app"
    // Play exige targetSdk 36. Estas AndroidX piden compileSdk 37; no sube el target.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.guiaheartopia.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 41
        versionName = "1.0.8"
        buildConfigField(
            "String",
            "CONTENT_BASE_URL",
            "\"https://anibalortizdev.github.io/guia-heartopia-contenido/\"",
        )
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
        if (distroSigningReady) {
            create("distro") {
                storeFile = distroKeystore
                storePassword = "wikifan-dist"
                keyAlias = "wikifan"
                keyPassword = "wikifan-dist"
            }
        }
        if (uploadSigningReady) {
            create("upload") {
                storeFile = uploadKeystore
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // ID oficiales de prueba de Google. Solo esta variante.
            buildConfigField("String", "ADMOB_APP_ID", "\"$testAdMobApp\"")
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$testAdMobBanner\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT_ID", "\"$testAdMobInterstitial\"")
            buildConfigField("String", "ADMOB_REWARDED_CALENDAR_UNIT_ID", "\"$testAdMobRewarded\"")
            buildConfigField("String", "ADMOB_REWARDED_ADFREE_UNIT_ID", "\"$testAdMobRewarded\"")
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField("boolean", "ADS_USE_GOOGLE_TEST_IDS", "true")
            val geo = secret("UMP_DEBUG_GEOGRAPHY")
            val device = secret("UMP_TEST_DEVICE_ID")
            buildConfigField("String", "UMP_DEBUG_GEOGRAPHY", "\"${geo.replace("\"", "")}\"")
            buildConfigField("String", "UMP_TEST_DEVICE_ID", "\"${device.replace("\"", "")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField(
                "String",
                "ADMOB_APP_ID",
                "\"${effectiveAdId(releaseAppId, testAdMobApp, releaseUseTestAdIds).replace("\"", "")}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_BANNER_UNIT_ID",
                "\"${effectiveAdId(releaseBannerId, testAdMobBanner, releaseUseTestAdIds).replace("\"", "")}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_UNIT_ID",
                "\"${effectiveAdId(releaseInterstitialId, testAdMobInterstitial, releaseUseTestAdIds).replace("\"", "")}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_REWARDED_CALENDAR_UNIT_ID",
                "\"${effectiveAdId(releaseRewardedCalendarId, testAdMobRewarded, releaseUseTestAdIds).replace("\"", "")}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_REWARDED_ADFREE_UNIT_ID",
                "\"${effectiveAdId(releaseRewardedAdFreeId, testAdMobRewarded, releaseUseTestAdIds).replace("\"", "")}\"",
            )
            buildConfigField("boolean", "ADS_ENABLED", releaseAdsEnabled.toString())
            buildConfigField("boolean", "ADS_USE_GOOGLE_TEST_IDS", releaseUseTestAdIds.toString())
            buildConfigField("String", "UMP_DEBUG_GEOGRAPHY", "\"\"")
            buildConfigField("String", "UMP_TEST_DEVICE_ID", "\"\"")
            signingConfig = when {
                releaseSigningReady -> signingConfigs.getByName("release")
                uploadSigningReady -> signingConfigs.getByName("upload")
                distroSigningReady -> signingConfigs.getByName("distro")
                else -> signingConfigs.getByName("debug")
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
        jniLibs {
            useLegacyPackaging = false
        }
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
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
