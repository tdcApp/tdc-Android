# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# ─── Preserve Line Numbers for Play Console Crash Reporting ───
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ─── TDC Data Models, Entities, and DTOs ───
-keepclassmembers class com.bagadbille.tdc.data.model.** { *; }
-keepclassmembers class com.bagadbille.tdc.data.remote.dto.** { *; }
-keepclassmembers class com.bagadbille.tdc.data.local.entity.** { *; }

# ─── Crypto Keystore ───
-keep class com.bagadbille.tdc.util.CryptoManager { *; }

# ─── Strip Logcat Output in Production Release Builds ───
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ─── Kotlinx Serialization ───
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <methods>;
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static *** INSTANCE;
}

# ─── Retrofit ───
-keepattributes Signature, Exceptions
-dontwarn javax.annotation.**
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ─── OkHttp ───
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ─── Room Database ───
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ─── Coroutines ───
-dontwarn kotlinx.coroutines.**