# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.google.common.util.concurrent.ListenableFuture
-dontwarn com.google.gson.Gson
-dontwarn com.tencent.smtt.sdk.ValueCallback

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.tencent.qimei.codez.FalconSdk
-dontwarn com.tencent.qimei.codez.IFalconSdk
-dontwarn com.tencent.qimei.codez.shell.UserInfoType
-dontwarn com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension
-dontwarn com.tencent.smtt.sdk.WebSettings
-dontwarn com.tencent.smtt.sdk.WebView
-dontwarn com.tencent.smtt.sdk.WebViewClient
-dontwarn com.tencentcloudapi.cls.android.producer.AsyncProducerClient
-dontwarn com.tencentcloudapi.cls.android.producer.AsyncProducerConfig
-dontwarn com.tencentcloudapi.cls.android.producer.Callback
-dontwarn com.tencentcloudapi.cls.android.producer.common.LogItem
-dontwarn com.tencentcloudapi.cls.android.producer.errors.ProducerException