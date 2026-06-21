-keep class com.seance.tv.data.model.** { *; }
-keep class is.xyz.mpv.** { *; }
-keepclassmembers class * {
    @com.google.dagger.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**
