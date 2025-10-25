# app/proguard-rules.pro
# Add project specific ProGuard rules here.

# Keep your package structure
-keep class com.novacpaymen.paywithnovac_android_skd.** { *; }

# Keep Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# GSON
-keep class com.google.gson.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Don't warn about anything in the library
-dontwarn com.novacpaymen.paywithnovac_android_skd.**