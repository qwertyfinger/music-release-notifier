# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\qwertyfinger\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:


##---------------Begin: proguard configuration common for all Android apps ----------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-allowaccessmodification
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-repackageclasses ''

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
  public static <fields>;
}

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class * {
    public protected *;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
##---------------End: proguard configuration common for all Android apps ----------

-dontwarn com.squareup.okhttp.**
-dontwarn repackaged.org.apache.http.**
-dontwarn org.musicbrainz.**
-dontwarn org.jdom.xpath.**
-dontwarn org.jdom.**
-dontwarn org.apache.commons.logging.**
-dontwarn okio.**

-libraryjars libs

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }
-keep class com.squareup.okio.** { *; }
-keep interface com.squareup.okio.** { *; }
-keep class com.squareup.okio.** { *; }
-keep interface com.squareup.okio.** { *; }
-keep class de.greenrobot.event.** { *; }
-keep interface de.greenrobot.event.** { *; }
-keep class se.emilsjolander.stickylistheaders.** { *; }
-keep interface se.emilsjolander.stickylistheaders.** { *; }
-keep class com.path.android.jobqueue.** { *; }
-keep interface com.path.android.jobqueue.** { *; }
-keep class com.squareup.picasso.** { *; }
-keep interface com.squareup.picasso.** { *; }
-keep class com.astuetz.** { *; }
-keep interface com.astuetz.** { *; }

-keep class org.apache.commons.logging.** { *; }
-keep interface org.apache.commons.logging.** { *; }
-keep class org.apache.commons.lang3.** { *; }
-keep interface org.apache.commons.lang3.** { *; }
-keep class org.jdom.** { *; }
-keep interface org.jdom.** { *; }
-keep class de.umass.** { *; }
-keep interface de.umass.** { *; }
-keep class org.mc2.** { *; }
-keep interface org.mc2.** { *; }
-keep class org.musicbrainz.** { *; }
-keep interface org.musicbrainz.** { *; }
-keep class repackaged.org.apache.http.** { *; }
-keep interface repackaged.org.apache.http.** { *; }
-keep class repackaged.org.apache.http.** { *; }
-keep interface repackaged.org.apache.http.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class title to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
