# sing-box libbox
-keep class io.nekohasekai.libbox.** { *; }
-dontwarn io.nekohasekai.libbox.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
