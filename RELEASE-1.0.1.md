# Parking Escape 1.0.1 / code 3

Replaces obsolete transitive AndroidX Fragment 1.1.0 with 1.8.9 and updates Google Mobile Ads within the 24.x series, from 24.6.0 to 24.9.0. UMP 4.0.0 remains unchanged. The min SDK stays 24. Native bridge, consent flow, ad IDs, level data and stored progress format are unchanged. Corrects two JavaScript syntax errors in the shipped index.html (a stray minus in daily() and the missing closing brace of finish()) and explicitly initializes dailyMode. Those errors prevented startup; the prior signed AAB contains the same faulty asset.

CI pins Gradle 8.9, verifies Fragment in both runtime graphs, saves the selected release dependency graph, runs Android lint and test tasks, builds APK/AAB, and signs the AAB with the existing upload key. No app unit tests existed at the base revision; a NO-SOURCE test task must not be reported as passing unit tests.

Before release: verify signed manifest/version/certificate against the previous Play package, exact tested game asset equality, and actual phone behavior including consent, no-fill, rewarded hints, interstitials, and progress retained on update. This dependency update alone does not prove that Play Console has cleared its SDK warning.

Official release notes:
- https://developer.android.com/jetpack/androidx/releases/fragment#1.8.9
- https://developers.google.com/admob/android/rel-notes

Production access application: the Console now shows the closed testing requirements complete. Prepare factual answers from real tester recruitment, feedback and changes; do not invent those details or submit a new dependency build as phone-tested.

Browser regression covers saved coins/stars/unlocks, 150 level buttons and locked levels, real pointer dragging, undo/restart, normal win and reward, daily challenge startup, garage, stubbed rewarded bridge outcomes and page errors at three mobile sizes. Real ads and native WebView remain phone-test items.
