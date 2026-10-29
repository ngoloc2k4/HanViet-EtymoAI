# ProGuard rules for HanViet-EtymoAI
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn org.jetbrains.annotations.**
