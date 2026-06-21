-keep class com.seance.tv.data.model.** { *; }
-keep class androidx.media3.** { *; }
-keepclassmembers class * {
    @com.google.dagger.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**
