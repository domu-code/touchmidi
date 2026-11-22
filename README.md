# TouchMIDI

Android-native, on-device editable MIDI control surface. No computer-side editor required.

## Download / install

Open [Releases](https://github.com/domu-code/touchmidi/releases/latest) and download **TouchMIDI-debug.apk**. Enable installation from this source when Android prompts you. These CI debug builds are signed with the default debug key, not a production signing key. If releases are unavailable, inspect the [Android APK workflow](https://github.com/domu-code/touchmidi/actions/workflows/android.yml).

## Features

- **Edit / Play**: touch to select, drag to position, resize with lower-right corner. All geometry locks in Play.
- **Six control types**: rotary knob, vertical fader, horizontal slider, momentary button, toggle, dual-CC XY pad.
- **MIDI messages**: Control Change, Note On/Off, Program Change, Pitch Bend, Channel Pressure, Polyphonic Pressure. All controls have a MIDI channel, assignable number, value range and accent colour.
- **Stepped knobs**: optional discrete CC values and text labels (e.g. filter and FX type).
- **Multiple pages**: create, switch, rename, duplicate, delete.
- **Project memory**: autosave in Android local preferences, export/import JSON via Android document picker.
- **Multitouch**: independent finger gestures in Play, note-off on release, MIDI panic.
- **USB MIDI and Android MIDI virtual ports** using the native Android MidiManager API. Android-discovered Bluetooth MIDI ports are also selectable if exposed by the operating system or another service.

## NTS-1 mk1 preset

The app starts with **KORG NTS-1 MK1**, containing the complete **29 Control Change messages documented by Korg** (OSC, filter, envelope, tremolo, modulation, delay, reverb, arpeggiator) across the OSC/FILTER, MOD/FX and ARP/KEYS pages. It also includes 12 chromatic Note buttons and Pitch Bend. Type selectors are quantised to the Korg CC steps, with readable labels.

The original hardware recognises MIDI clock, selected SysEx and other system messages as well. Clock transmission and SysEx editing are not implemented in this initial version. This is a full *documented CC parameter* preset, not a complete SysEx editor.

Official reference: [Korg NTS-1 digital kit MIDI Implementation Chart (2019)](https://www.korg.com/us/support/download/manual/0/832/4370/). Enable **MIDI RX ShortMessage** in the NTS-1 global parameters and match the MIDI channel. Use a USB OTG MIDI connection for the most direct setup.

## Android project

Pure Java, no external Java libraries and no desktop editor dependency. Android 8.0+ (API 26). Build with JDK 17, Gradle 8.9 and Android SDK 35:

```sh
gradle :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

Continuous integration automatically compiles the project when changes land on main and creates a GitHub release containing the sideloadable debug APK.

## Limitations

This is an initial Android alpha. Connection to physical MIDI hardware has not been verified on a device, and there is no automated Android instrumented test suite. External Bluetooth LE MIDI scanning/pairing, feedback from MIDI devices, custom SysEx, and a clock generator are future enhancements. Export your JSON layouts before reinstalling: app storage can be removed on uninstall.
