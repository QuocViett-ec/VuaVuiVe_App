# ProGuard rules for consumers of the shared library
# Keep all public DTOs
-keep class vn.vuavuive.shared.data.dto.** { *; }
-keep class vn.vuavuive.shared.data.api.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
