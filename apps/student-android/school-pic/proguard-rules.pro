# Tentuin School PIC — proguard rules

# Keep model classes used with Gson reflection
-keep class id.tentuin.schoolpic.data.model.** { *; }

# Retrofit / OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
