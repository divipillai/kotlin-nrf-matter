# Viewing logs

The Logs Panel shows a single combined log for the whole app, covering commissioning, cluster reads
and writes, and binding operations. It is the first place to look when an accessory does not respond.

Logs are persisted, so they remain available after the app is restarted. On Android they are stored
in a local database; on iOS they come from the logger in the vendored `ios-matter` package, shared
with the commissioning app extension.

<div align="center">
  <img src="./screenshots/logs_panel.png" alt="Logs Panel" />
</div>

## User interface

| UI element | Description |
| --- | --- |
| Search field | Filters the log by message text, case-insensitively. The clear icon at the end of the field resets the search. |
| Level filter chips | **ALL**, **INFO**, **DEBUG**, and **ERROR** restrict the log to the selected levels. |
| Log view | The filtered entries, newest last. When nothing matches, it reads **No logs match current search/filter.** |

Each entry shows its timestamp on the left and its level — with the originating tag, where one is
set — on the right, above the message. Levels are colour-coded:

| Level | Colour |
| --- | --- |
| `INFO` | Green |
| `DEBUG` | Blue |
| `ERROR` | Red |

## Exporting logs

!!! note "Note"

    The log cannot be cleared or exported from within the app. To share a trace, capture it from the
    device using the platform tooling, such as `adb logcat` on Android or the Console app on macOS
    for iOS.
