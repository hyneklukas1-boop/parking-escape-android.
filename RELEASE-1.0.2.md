# Parking Escape 1.0.2 / code 4

Fixes game controls and level information overlapping the phone status bar,
reported in the user's screenshot of level 27 on 24 October 2026.

The native WebView is hosted inside a FrameLayout that applies system bar and
display cutout insets on all four sides. Insets are consumed before reaching
the WebView, and the legacy immersive flags and focus/resume re-hiding are
removed. System bars remain visible with light icons. The game asset, saved
progress format, levels, consent flow and ad units are unchanged from 1.0.1.

AndroidX Core is declared directly at the already resolved version 1.10.1.
Min SDK 24, target/compile SDK 36. Upload signing uses the existing CI secrets.

CI 40091135296 passed: three mobile browser scenarios, dependency verification,
Android build and lint (0 errors, 7 warnings). The native unit task has NO-SOURCE.
The signed 1.0.2/code 4 AAB passes bundletool validation and jar signature
verification with the existing upload certificate. Game asset bytes match 1.0.1.

The browser regression does not test native Android window insets. After the
signed build is installed on a phone, verify header
clearance, gesture and three-button navigation, rotation, saved progress,
return from an ad, and normal vehicle dragging before public release.
