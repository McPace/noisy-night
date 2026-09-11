# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/pace/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

# Keep kotlin serialization if used
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Service
-keep class com.papapace.noisynight.NoiseAudioService { *; }
