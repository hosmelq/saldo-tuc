# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/hosmel/Library/Android/sdk/tools/proguard/proguard-android.txt
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

-keepattributes *Annotation*

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** d(...);
    public static *** e(...);
    public static *** i(...);
    public static *** v(...);
    public static *** w(...);
}

-assumenosideeffects class com.socialimprover.saldotuc.util.LogUtils {
    public static *** LOGD(...);
    public static *** LOGV(...);
    public static *** LOGI(...);
    public static *** LOGW(...);
    public static *** LOGE(...);
}