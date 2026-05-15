# Add project specific ProGuard rules here.
# For Retrofit + Gson model classes
-keep class id.tentuin.admin.data.model.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
