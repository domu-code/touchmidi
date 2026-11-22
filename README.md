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
- **Safe bottom controls**: MIDI destination and EDIT/PLAY buttons have been moved away from the Android notification shade gesture area.
- **USB MIDI and Android MIDI virtual ports** using the native Android MidiManager API. Android-discovered Bluetooth MIDI ports are also selectable if exposed by the operating system or another service.

## On-device piano roll

Tap **PIANO ROLL** in the fixed bottom toolbar. It opens a distinct scrolling note editor and loop sequencer. The timeline can be resized from 1 to 1,000,000 sixteenth-note steps; the view only renders the currently visible cells. Subtle gridlines mark individual steps, medium lines every **4**, and strong lines every **8**. Each 16 steps represents one 4/4 bar.

- Tap an empty cell to add a note; tap a note to delete it.
- Drag a note to move it or drag its right edge to resize its duration.
- Drag the top ruler to pan horizontally; drag the piano keys on the left to scroll octaves.
- Set BPM (20–300), channel (1–16), note velocity, sequence length, and horizontal zoom.
- Start/stop loop playback, with MIDI Note On/Off on an Android MIDI output port.
- All note data is saved with project JSON and included in exports.

The sequencer runs on Android's main-thread scheduler and is suitable for experimentation, not guaranteed sample-accurate timing. External MIDI clock, recording live MIDI into the roll, velocity lanes, and multiple independent roll patterns are planned improvements.

## LOOM preset (M7.7.0)

Open **PROJECT → Load LOOM M7.7**. Five edit-ready CC pages follow LOOM's real Plaits/Braids, filter/mixer, DMG/sampler, FX/Beads, Tides and Rings/Air parameters. A performance page adds keyboard, mod wheel (CC1), pitch bend, and sustain (CC64). This version of LOOM has **MIDI learn**, not fixed factory CC numbers. The suggested CC20–80 control assignments therefore require learning in LOOM by right-clicking the corresponding parameter and moving its TouchMIDI control. See [LOOM CC Learn Map](docs/LOOM_CC_MAP.md).

## MetroGaia preset (v6.9)

Open **PROJECT → Load MetroGaia v6.9**. The preset reproduces MetroGaia's *incoming* motion range CCs 20–35, gate/toggle CCs 60–63 and mod wheel CC1. Four XY pads use CC44–51, which are MetroGaia's *outgoing* XY motion defaults. Those XY pads do **not** directly control MetroGaia's internal parameters without MIDI routing or additional mapping. The preset includes keyboard, pitch bend and pressure controllers. See [MetroGaia MIDI Map](docs/METROGAIA_MIDI_MAP.md).

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

This is an early Android alpha. Connection to physical MIDI hardware has not been verified on a device, and there is no automated Android instrumented test suite. External Bluetooth LE MIDI scanning/pairing, feedback from MIDI devices, custom SysEx, and a clock generator are future enhancements. Export your JSON layouts before reinstalling: app storage can be removed on uninstall.
