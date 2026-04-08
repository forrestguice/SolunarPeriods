~

### v0.2.2 (2026-04-08)
* adds support for "Material You" (needs Suntimes v0.17.0+).
* adds exception handler and crash report notification.
* adds "online help" button (https://forrestguice.github.io/Suntimes/help/addons/solunarperiods).
* updates build; targetSdkVersion 33 -> 34; Gradle 6.5 -> 8.7; Android Gradle Plugin 4.1.3 -> 8.5.2.
* updates build; adds nightly build flavor (#9).

### v0.2.1 (2023-08-21)
* adds support for system theme (night mode), high contrast app themes, and "text size" settings.
* fixes crash when using "calendar integration" menu item (#6).
* updates build; targetSdkVersion 28 -> 33; Gradle 4.4 -> 6.5; Android Gradle Plugin 3.1.3 -> 4.1.3; migrates from legacy support libraries to AndroidX.

### v0.2.0 (2020-12-31)
* adds "Calendar Integration" (#1). This feature requires "Suntimes Calendars" v0.5.0.
* adds bottom sheet; shows an expanded view with note explaining the reasons for a daily rating.
* adds "Share" menu item; shares a daily rating as text.
* adds "Go to..." menu item; scrolls to a given date.
* adds "View Date" menu item; opens Suntimes to a given date.
* adds Time Zone selector; "System", "Suntimes", "Apparent Solar", and "Local Mean".
* modifies the range of cards to +-10 years.

### v0.1.0 (2020-03-16) [First Release]
* an add-on app that uses the current Suntimes configuration (location, timezone, theme, locale, and UI options). The minimum Suntimes version is `v0.10.3` (but without access to UI options); the recommended version is `v0.12.6`.
* UI provides an ActionBar that displays configured Location (lat, lon, alt), Suntimes icon (opens Suntimes activity), and overflow menu (Help, and About).
* UI provides a BottomBar that displays the configured timezone. 
* UI provides a RecyclerView that displays a series of cards (days centered on today); each card displays the date, sunrise, sunset, moonrise and moonset (minor periods), lunar noon and lunar midnight (major periods), moon phase, illumination, and a daily prediction (average, good, better, best).
* UI provides a FloatingActionButton that (re)centers the RecyclerView on "Today".
* UI provides an indicator that is displayed next to periods that coincide with sunrise/sunset.