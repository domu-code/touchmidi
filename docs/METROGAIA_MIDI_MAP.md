# MetroGaia v6.9 MIDI mapping in TouchMIDI

Verified against `MetroGaia_v6_9/MetroGaia_v6_9.pde`, specifically `sliderResponseCC`, `sliderNames`, `controllerChange`, and `channelXCC/channelYCC`.

## Incoming mapping used by MetroGaia

| Page | MIDI CC | Target |
| --- | --- | --- |
| RANGES 1–4 | 20 | Channel 1 X MIN |
| RANGES 1–4 | 21 | Channel 1 X MAX |
| RANGES 1–4 | 22 | Channel 1 Y MIN |
| RANGES 1–4 | 23 | Channel 1 Y MAX |
| RANGES 1–4 | 24 | Channel 2 X MIN |
| RANGES 1–4 | 25 | Channel 2 X MAX |
| RANGES 1–4 | 26 | Channel 2 Y MIN |
| RANGES 1–4 | 27 | Channel 2 Y MAX |
| RANGES 5–8 | 28 | Channel 3 X MIN |
| RANGES 5–8 | 29 | Channel 3 X MAX |
| RANGES 5–8 | 30 | Channel 3 Y MIN |
| RANGES 5–8 | 31 | Channel 3 Y MAX |
| RANGES 5–8 | 32 | Channel 4 X MIN |
| RANGES 5–8 | 33 | Channel 4 X MAX |
| RANGES 5–8 | 34 | Channel 4 Y MIN |
| RANGES 5–8 | 35 | Channel 4 Y MAX |
| GATES / KEYS | 60 | Gate toggle 1 |
| GATES / KEYS | 61 | Gate toggle 2 |
| GATES / KEYS | 62 | Gate toggle 3 |
| GATES / KEYS | 63 | Gate toggle 4 |
| EXPRESSION | 1 | Mod wheel |

The 16 motion range assignments above are the source's `sliderResponseCC` defaults (CC20–35), and can be changed in the MetroGaia options. This controller layout assumes the defaults are unchanged.

## XY pads and MIDI routing

MetroGaia's **outgoing MIDI** motion recorder defaults to X/Y pairs `44/45`, `46/47`, `48/49`, `50/51`. TouchMIDI's four XY pads mirror those assignments. They send MIDI messages, but MetroGaia **does not treat CC44–51 as direct synthesizer parameter controls** out of the box. Connect those messages via an external routing service or remap the controls to valid receive CCs when desired.

MIDI Note On/Off, Pitch Bend and Channel Pressure are also available on TouchMIDI's expression/performance pages.
