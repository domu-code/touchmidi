# LOOM M7.7.0 / TouchMIDI CC learn layout

Based on the source parameter keys of Loom_M7_7_0_ModularCV.pde. **LOOM has no preset fixed CC table**: MIDI learn must be used for each parameter. CC assignments in this document are proposed for TouchMIDI, not LOOM defaults.

In LOOM, right-click the named parameter to arm MIDI learn, then move its TouchMIDI control; ensure the CC input listens on the chosen Android/virtual MIDI channel. In the current LOOM engine a learned CC is an **additive bipolar modulation** centred around value 64, and depth is configurable, not absolute parameter override. Adjust MIDI depth in LOOM if the behaviour is stronger/weaker than intended.

| Page | CC | Parameter |
| --- | --- | --- |
| PL / BRAIDS | 20 | PL HARMONICS |
| PL / BRAIDS | 21 | PL TIMBRE |
| PL / BRAIDS | 22 | PL MORPH |
| PL / BRAIDS | 23 | PL LEVEL |
| PL / BRAIDS | 24 | BR HARMONICS |
| PL / BRAIDS | 25 | BR TIMBRE |
| PL / BRAIDS | 26 | BR MORPH |
| PL / BRAIDS | 27 | BR LEVEL |
| PL / BRAIDS | 28 | PL DECAY |
| PL / BRAIDS | 29 | PL COLOUR |
| PL / BRAIDS | 30 | BR DECAY |
| PL / BRAIDS | 31 | BR COLOUR |
| FILTER / MIX | 32 | CUTOFF |
| FILTER / MIX | 33 | RESONANCE |
| FILTER / MIX | 34 | FILTER MODE |
| FILTER / MIX | 35 | FILTER MORPH |
| FILTER / MIX | 36 | FILTER SLOPE |
| FILTER / MIX | 37 | FILTER WET |
| FILTER / MIX | 38 | PL PAN |
| FILTER / MIX | 39 | BR PAN |
| FILTER / MIX | 40 | DMG LEVEL |
| FILTER / MIX | 41 | SAMPLE LEVEL |
| FILTER / MIX | 42 | RINGS LEVEL |
| FILTER / MIX | 43 | MASTER |
| DMG / SAMPLE | 44 | DMG WAVE SCAN |
| DMG / SAMPLE | 45 | DMG WIDTH |
| DMG / SAMPLE | 46 | DMG JITTER |
| DMG / SAMPLE | 47 | DMG FOLD |
| DMG / SAMPLE | 48 | DMG FILTER CUT |
| DMG / SAMPLE | 49 | DMG FILTER RES |
| DMG / SAMPLE | 50 | DMG PAN |
| DMG / SAMPLE | 51 | SAMPLE PAN |
| DMG / SAMPLE | 52 | GRAIN SIZE |
| DMG / SAMPLE | 53 | GRAIN DENSITY |
| DMG / SAMPLE | 54 | SAMPLE FINE |
| DMG / SAMPLE | 55 | DMG WRAP |
| FX / BEADS | 56 | DRIVE MIX |
| FX / BEADS | 57 | CHORUS RATE |
| FX / BEADS | 58 | CHORUS DEPTH |
| FX / BEADS | 59 | CHORUS MIX |
| FX / BEADS | 60 | DELAY TIME |
| FX / BEADS | 61 | DELAY FEEDBACK |
| FX / BEADS | 62 | DELAY MIX |
| FX / BEADS | 63 | BEADS TIME |
| FX / BEADS | 65 | BEADS SIZE |
| FX / BEADS | 66 | BEADS DENSITY |
| FX / BEADS | 67 | BEADS MIX |
| FX / BEADS | 68 | REVERB MIX |
| TIDES / VOICES | 69 | TIDES 1 RATE |
| TIDES / VOICES | 70 | TIDES 1 SHAPE |
| TIDES / VOICES | 71 | TIDES 1 SLOPE |
| TIDES / VOICES | 72 | TIDES 1 SMOOTH |
| TIDES / VOICES | 73 | TIDES 2 RATE |
| TIDES / VOICES | 74 | TIDES 2 SHAPE |
| TIDES / VOICES | 75 | TIDES 2 SLOPE |
| TIDES / VOICES | 76 | TIDES 2 SMOOTH |
| TIDES / VOICES | 77 | RINGS STRUCT |
| TIDES / VOICES | 78 | RINGS BRIGHT |
| TIDES / VOICES | 79 | RINGS DAMPING |
| TIDES / VOICES | 80 | AIR BREATH |
| PERFORMANCE | 1 | MOD WHEEL (fixed CC1) |
| PERFORMANCE | 64 | SUSTAIN (fixed CC64) |

Keyboard notes use MIDI Note On/Off C4–B4. Pitch bend uses MIDI Pitch Bend messages.

Note: This is a *curated control surface*, not an exhaustive representation of LOOM's hundreds of parameters. Use Edit/Inspect to create more controls and map any of the remaining parameters by MIDI learn.
