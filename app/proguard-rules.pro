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

# mediapipe stuff
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

-keep class com.google.common.flogger.** { *; }

-keep public interface com.google.mediapipe.framework.* {
    public *;
}

-keep public class com.google.mediapipe.framework.Packet {
    public static *** create(***);
    public long getNativeHandle();
    public void release();
}

-keep public class com.google.mediapipe.framework.PacketCreator {
    *** releaseWithSyncToken(...);
}

-keep public class com.google.mediapipe.framework.MediaPipeException {
    <init>(int, byte[]);
}

-keep class com.google.mediapipe.framework.ProtoUtil$SerializedMessage {
    *;
}

-dontwarn aQute.bnd.annotation.spi.ServiceProvider