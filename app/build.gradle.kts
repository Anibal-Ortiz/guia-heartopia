import java.io.File
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Node

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/** Única fuente de la versión: nombre de archivo, versionName y artefacto de CI se derivan de ella. */
val appVersion = "1.1.4"

private val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private val TOOLS_NS = "http://schemas.android.com/tools"

private fun Node.attr(ns: String, name: String): String? =
    attributes?.getNamedItemNS(ns, name)?.nodeValue

/** Genera el manifiesto de release en fase de ejecución, nunca al configurar. */
abstract class GenerateReleaseManifestTask : DefaultTask() {
    @get:Input
    abstract val adsEnabled: Property<Boolean>

    @get:Input
    abstract val effectiveAppId: Property<String>

    @get:OutputFile
    abstract val releaseManifest: RegularFileProperty

    @TaskAction
    fun generate() {
        val output = releaseManifest.get().asFile
        output.parentFile.mkdirs()
        val enabled = adsEnabled.get()
        val appId = effectiveAppId.get()
        output.writeText(
            if (enabled) {
                """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                    <application>
                        <meta-data
                            android:name="com.google.android.gms.ads.APPLICATION_ID"
                            android:value="$appId" />
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
    }
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
val releaseRewardedAlarmId = secret("ADMOB_REWARDED_ALARM_AD_UNIT_ID")
val releaseAdsReady = releaseAppId.isNotEmpty() && releaseBannerId.isNotEmpty()
val releaseUseRealAdIds = !admobPlaceholder(releaseAppId) && !admobPlaceholder(releaseBannerId)
val releaseUseTestAdIds = !releaseUseRealAdIds
val releaseAdsEnabled = releaseUseTestAdIds || releaseAdsReady

fun effectiveAdId(real: String, test: String, useTest: Boolean): String =
    if (useTest) test else real.trim().takeUnless { admobPlaceholder(it) }.orEmpty()

val effectiveReleaseAppId = effectiveAdId(releaseAppId, testAdMobApp, releaseUseTestAdIds)

val releaseManifestFile = rootProject.file("app/generated-release-manifest/AndroidManifest.xml")

val generateReleaseManifest = tasks.register<GenerateReleaseManifestTask>("generateReleaseManifest") {
    adsEnabled.set(releaseAdsEnabled)
    effectiveAppId.set(effectiveReleaseAppId)
    releaseManifest.set(releaseManifestFile)
}

/** Comprobación barata del manifiesto generado, pensada para la CI (sin secretos). */
val verifyReleaseManifest = tasks.register("verifyReleaseManifest") {
    dependsOn(generateReleaseManifest)
    inputs.file(releaseManifestFile)
    doLast {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(releaseManifestFile)
        check(document.documentElement.tagName == "manifest") { "La raíz del manifiesto no es <manifest>" }
        val applications = document.getElementsByTagName("application")
        check(applications.length == 1) { "El manifiesto de release debe tener un solo <application>" }

        val metaData = document.getElementsByTagName("meta-data")
        val hasAppId = (0 until metaData.length).any {
            metaData.item(it).attr(ANDROID_NS, "name") == "com.google.android.gms.ads.APPLICATION_ID"
        }
        check(hasAppId == releaseAdsEnabled) {
            "APPLICATION_ID no coincide con ADS_ENABLED ($releaseAdsEnabled)"
        }
        if (!releaseAdsEnabled) {
            val providers = document.getElementsByTagName("provider")
            val providerRemoved = (0 until providers.length).any {
                providers.item(it).attr(ANDROID_NS, "name") ==
                    "com.google.android.gms.ads.MobileAdsInitProvider" &&
                    providers.item(it).attr(TOOLS_NS, "node") == "remove"
            }
            check(providerRemoved) { "Falta tools:node=\"remove\" para MobileAdsInitProvider" }
        }
    }
}

val storeFilePath = secret("RELEASE_STORE_FILE")
val releaseStorePassword = secret("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = secret("RELEASE_KEY_ALIAS")
val releaseKeyPassword = secret("RELEASE_KEY_PASSWORD")
val releaseSigningReady = storeFilePath.isNotEmpty() && releaseStorePassword.isNotEmpty() &&
    releaseKeyAlias.isNotEmpty() && releaseKeyPassword.isNotEmpty()

// La distro no tiene contraseñas en el repositorio: se leen como release/upload.
// Si el keystore existe pero faltan secretos, solo se falla cuando se firma distro.
val distroKeystore = File(System.getProperty("user.home"), ".wikifan-heartopia/release.jks")
val distroStorePassword = secret("DISTRO_STORE_PASSWORD")
val distroKeyAlias = secret("DISTRO_KEY_ALIAS")
val distroKeyPassword = secret("DISTRO_KEY_PASSWORD")
val distroSigningReady = distroKeystore.isFile &&
    distroStorePassword.isNotEmpty() && distroKeyAlias.isNotEmpty() && distroKeyPassword.isNotEmpty()

val allowDebugSignedRelease = providers.gradleProperty("allowDebugSignedRelease")
    .orNull?.toBoolean() ?: false
val releaseSigningRequested = gradle.startParameter.taskNames.any { task ->
    val simple = task.substringAfterLast(":")
    simple in setOf("build", "assemble", "bundle") ||
        simple.startsWith("assembleRelease") ||
        simple.startsWith("bundleRelease") ||
        simple.startsWith("installRelease") ||
        simple.startsWith("publish")
}

fun failReleaseSigning(message: String) {
    if (releaseSigningRequested) throw GradleException(message)
}

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
    archivesName.set("Guia-Heartopia-$appVersion")
}

android {
    namespace = "com.guiaheartopia.app"
    // Play exige targetSdk 36. Estas AndroidX piden compileSdk 37; no sube el target.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.guiaheartopia.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 47
        versionName = appVersion
        buildConfigField(
            "String",
            "CONTENT_BASE_URL",
            "\"https://anibalortizdev.github.io/guia-heartopia-contenido/\"",
        )
        // ADS_CONFIGURED se fija por variante en buildTypes: true = IDs sin placeholders.
        // AdMobIds.kt podría usar BuildConfig.ADS_CONFIGURED en vez de detectar marcadores en runtime.
        buildConfigField("boolean", "ADS_CONFIGURED", "false")
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
                storePassword = distroStorePassword
                keyAlias = distroKeyAlias
                keyPassword = distroKeyPassword
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
            buildConfigField("String", "ADMOB_REWARDED_ALARM_UNIT_ID", "\"$testAdMobRewarded\"")
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField("boolean", "ADS_USE_GOOGLE_TEST_IDS", "true")
            // En debug los IDs oficiales de prueba cuentan como configurados.
            buildConfigField("boolean", "ADS_CONFIGURED", "true")
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
            buildConfigField(
                "String",
                "ADMOB_REWARDED_ALARM_UNIT_ID",
                "\"${effectiveAdId(releaseRewardedAlarmId, testAdMobRewarded, releaseUseTestAdIds).replace("\"", "")}\"",
            )
            buildConfigField("boolean", "ADS_ENABLED", releaseAdsEnabled.toString())
            buildConfigField("boolean", "ADS_USE_GOOGLE_TEST_IDS", releaseUseTestAdIds.toString())
            // true solo con IDs reales: equivale a la detección de placeholders de admobPlaceholder().
            buildConfigField("boolean", "ADS_CONFIGURED", releaseUseRealAdIds.toString())
            buildConfigField("String", "UMP_DEBUG_GEOGRAPHY", "\"\"")
            buildConfigField("String", "UMP_TEST_DEVICE_ID", "\"\"")
            // Nunca firmar release con el keystore de debug en silencio.
            signingConfig = when {
                releaseSigningReady -> signingConfigs.getByName("release")
                uploadSigningReady -> signingConfigs.getByName("upload")
                distroSigningReady -> signingConfigs.getByName("distro")
                allowDebugSignedRelease -> signingConfigs.getByName("debug")
                else -> {
                    val reason = when {
                        distroKeystore.isFile ->
                            "Falta DISTRO_STORE_PASSWORD / DISTRO_KEY_ALIAS / DISTRO_KEY_PASSWORD " +
                                "para el keystore de distro en $distroKeystore"
                        else ->
                            "Faltan secretos de firma (RELEASE_* / UPLOAD / DISTRO). " +
                                "Usa -PallowDebugSignedRelease=true solo para una compilación local no publicable."
                    }
                    failReleaseSigning("No se puede firmar release: $reason")
                    signingConfigs.getByName("debug")
                }
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

// El manifiesto de release se genera antes de que AGP lo procese; debug no depende de esta tarea.
tasks.matching { task ->
    task.name.contains("Release", ignoreCase = true) &&
        task.name.startsWith("process") &&
        task.name.endsWith("Manifest")
}.configureEach {
    dependsOn(generateReleaseManifest)
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
    implementation(libs.play.app.update.ktx)
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
