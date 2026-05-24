# NavalBattle ProGuard. MVP — минификация выключена.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers,allowshrinking class * { @kotlinx.serialization.Serializable <methods>; }
-keep,includedescriptorclasses class com.elyssov.navalbattle.**$$serializer { *; }
-keepclassmembers class com.elyssov.navalbattle.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
