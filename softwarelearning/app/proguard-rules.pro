# ============================
# PROGUARD - REGRAS “FULL OPEN”
# ============================

# ----------------------------
# Modelos POJO do seu projeto
# ----------------------------
-keep class com.academic.softwarelearning.model.** { *; }

# ----------------------------
# Firebase & Google Play Services
# ----------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Firebase Tasks e Listeners
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.firebase.tasks.** { *; }

# Firebase Database & Firestore
-keep class com.google.firebase.database.** { *; }
-keep class com.google.firebase.firestore.** { *; }

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# ----------------------------
# Retrofit & OkHttp
# ----------------------------
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ----------------------------
# Gson
# ----------------------------
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ----------------------------
# Coroutines
# ----------------------------
-keep class kotlinx.coroutines.** { *; }

# ----------------------------
# LiveData, ViewModel, Activities & Fragments
# ----------------------------
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.appcompat.app.AppCompatActivity { *; }
-keep class androidx.fragment.app.Fragment { *; }

# ----------------------------
# Parcelable & Serializable
# ----------------------------
-keep class * implements android.os.Parcelable { *; }
-keep class * implements java.io.Serializable { *; }

# ----------------------------
# Reflection / Classes Dinâmicas
# ----------------------------
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    <init>(...);
    public <methods>;
}

-keepclassmembers class * {
    <fields>;
    <methods>;
}

# ----------------------------
# WebView JS interfaces
# ----------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ----------------------------
# Java Collections / Util
# ----------------------------
-keep class java.util.** { *; }
-keep class java.lang.** { *; }
-keep class java.math.** { *; }

# ----------------------------
# Evita warnings de bibliotecas
# ----------------------------
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.gson.**
-dontwarn kotlinx.coroutines.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn org.checkerframework.**

# ----------------------------
# Mantém atributos de depuração
# ----------------------------
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*