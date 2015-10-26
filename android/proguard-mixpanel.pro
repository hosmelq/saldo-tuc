-keep class com.mixpanel.android.abtesting.** { *; }
-keep class com.mixpanel.android.mpmetrics.** { *; }
-keep class com.mixpanel.android.surveys.** { *; }
-keep class com.mixpanel.android.util.** { *; }
-keep class com.mixpanel.android.java_websocket.** { *; }

-keepattributes InnerClasses

-dontwarn com.mixpanel.**
-keep class **.R
-keep class **.R$* { <fields>; }