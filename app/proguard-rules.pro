# R8 / shrinkResources activos en release.

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,InnerClasses,EnclosingMethod,Signature
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AdMob y UMP. Los AAR ya traen reglas de consumidor; esto cubre reflexión del SDK.
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.ads.**
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# WorkManager instancia el worker por reflexión.
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# kotlinx.serialization: conserva Companion.serializer() de los modelos @Serializable.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class com.guiaheartopia.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.guiaheartopia.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.guiaheartopia.app.**$$serializer { *; }
-keepclassmembers enum com.guiaheartopia.app.domain.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
