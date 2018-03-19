# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/wjielai/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-dontwarn com.tencent.bugly.**
-keep class com.tencent.bugly.** { *;}

-dontwarn org.apache.**
-keep class org.apache.** { *;}


-dontwarn com.tencent.midas.**
-keep class com.tencent.midas.** { *;}


-dontwarn com.tencent.openmidas.**
-keep class com.tencent.openmidas.** { *;}

-dontwarn com.tencent.stat.**
-keep class com.tencent.stat.** { *;}

-dontwarn com.tencent.smtt.**
-keep class com.tencent.smtt.** { *;}


-dontwarn com.tencent.tac.**
-keep class com.tencent.tac.** { *;}

-dontwarn org.**
-keep class org.** { *;}

-dontwarn com.tencent.qcloud.**
-keep class com.tencent.qcloud.** { *;}

-dontwarn com.tencent.android.**
-keep class com.tencent.android.** { *;}