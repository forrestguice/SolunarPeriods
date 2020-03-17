~

v0.1.0 (2020-03-16) [First Release]
* an add-on app that uses the current Suntimes configuration (location, timezone, theme, locale, and UI options). The minimum Suntimes version is `v0.10.3` (but without access to UI options); the recommended version is `v0.12.6`.
* UI provides an ActionBar that displays configured Location (lat, lon, alt), Suntimes icon (opens Suntimes activity), and overflow menu (Help, and About).
* UI provides a BottomBar that displays the configured timezone. 
* UI provides a RecyclerView that displays a series of cards (days centered on today); each card displays the date, sunrise, sunset, moonrise and moonset (minor periods), lunar noon and lunar midnight (major periods), moon phase, illumination, and a daily prediction (average, good, better, best).
* UI provides a FloatingActionButton that (re)centers the RecyclerView on "Today".
* UI provides an indicator that is displayed next to periods that coincide with sunrise/sunset.