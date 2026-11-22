package com.domucode.touchmidi;

import java.util.ArrayList;

public final class Presets {
    private Presets() {}

    private static Model.Control cc(Model.Page p, String label, int number, String kind, int col, int row, int cols, int rows, int color) {
        Model.Control c = new Model.Control();
        c.label = label;
        c.kind = kind; c.message = "CC"; c.number = number;
        c.color = color;
        c.x = .025f + col * (.95f / cols);
        c.y = .035f + row * (.93f / rows);
        c.w = .95f / cols - .012f;
        c.h = .93f / rows - .018f;
        c.value = 64;
        c.normalize(); p.controls.add(c);
        return c;
    }

    private static void choices(Model.Control c, int[] values, String... names) {
        c.choices = values;
        c.choiceNames = names;
        c.value = values[0];
    }

    public static Model.Project empty() {
        Model.Project p = new Model.Project();
        p.title = "NEW CONTROLLER";
        p.pages.add(new Model.Page("PAGE 1"));
        return p;
    }

    public static Model.Project nts1() {
        Model.Project project = new Model.Project();
        project.title = "KORG NTS-1 MK1";
        final int aqua = 0xff49d8c7;
        final int orange = 0xffffb56e;
        final int violet = 0xffbb9afa;
        final int blue = 0xff71b8ff;

        Model.Page sound = new Model.Page("OSC / FILTER");
        project.pages.add(sound);
        choices(cc(sound,"OSC TYPE",53,"KNOB",0,0,5,3,aqua),
                new int[]{0,25,50,75,127},"SAW","TRI","SQUARE","VPM","USER");
        cc(sound,"OSC SHAPE",54,"KNOB",1,0,5,3,aqua);
        cc(sound,"OSC ALT",55,"KNOB",2,0,5,3,aqua);
        cc(sound,"OSC LFO RATE",24,"KNOB",3,0,5,3,aqua);
        cc(sound,"OSC LFO DEPTH",26,"KNOB",4,0,5,3,aqua);

        choices(cc(sound,"FILTER TYPE",42,"KNOB",0,1,5,3,orange),
                new int[]{0,18,36,54,72,90,127},
                "LP2","LP4","BP2","BP4","HP2","HP4","OFF");
        cc(sound,"CUTOFF",43,"FADER",1,1,5,3,orange);
        cc(sound,"RESONANCE",44,"KNOB",2,1,5,3,orange);
        Model.Control sweep = cc(sound,"SWEEP DEPTH",45,"KNOB",3,1,5,3,orange);
        sweep.value = 63;
        cc(sound,"SWEEP RATE",46,"KNOB",4,1,5,3,orange);

        choices(cc(sound,"EG TYPE",14,"KNOB",0,2,5,3,violet),
                new int[]{0,25,50,75,127},
                "ADSR","AHR","AR","AR LOOP","OPEN");
        cc(sound,"EG ATTACK",16,"KNOB",1,2,5,3,violet);
        cc(sound,"EG RELEASE",19,"KNOB",2,2,5,3,violet);
        cc(sound,"TREMOLO DEPTH",20,"KNOB",3,2,5,3,violet);
        cc(sound,"TREMOLO RATE",21,"KNOB",4,2,5,3,violet);

        Model.Page fx = new Model.Page("MOD / FX");
        project.pages.add(fx);
        choices(cc(fx,"MOD TYPE",88,"KNOB",0,0,4,3,aqua),
                new int[]{0,25,50,75,127},
                "OFF","CHORUS","ENSEMBLE","PHASER","FLANGER");
        cc(fx,"MOD TIME",28,"KNOB",1,0,4,3,aqua);
        cc(fx,"MOD DEPTH",29,"KNOB",2,0,4,3,aqua);

        choices(cc(fx,"DELAY TYPE",89,"KNOB",0,1,4,3,orange),
                new int[]{0,21,42,63,84,127},
                "OFF","STEREO","MONO","PING PONG","HIGHPASS","TAPE");
        cc(fx,"DELAY TIME",30,"KNOB",1,1,4,3,orange);
        cc(fx,"DELAY DEPTH",31,"KNOB",2,1,4,3,orange);
        cc(fx,"DELAY MIX",33,"FADER",3,1,4,3,orange);

        choices(cc(fx,"REVERB TYPE",90,"KNOB",0,2,4,3,violet),
                new int[]{0,21,42,63,84,127},
                "OFF","HALL","PLATE","SPACE","RISER","SUBMARINE");
        cc(fx,"REVERB TIME",34,"KNOB",1,2,4,3,violet);
        cc(fx,"REVERB DEPTH",35,"KNOB",2,2,4,3,violet);
        cc(fx,"REVERB MIX",36,"FADER",3,2,4,3,violet);

        Model.Page arp = new Model.Page("ARP / KEYS");
        project.pages.add(arp);
        Model.Control pattern = cc(arp,"ARP PATTERN",117,"KNOB",0,0,4,2,blue);
        choices(pattern, new int[]{0,12,24,36,48,60,72,84,96,127},
                "UP","DOWN","UP-DOWN","DOWN-UP","CONV","DIV","CONV-DIV","DIV-CONV","RANDOM","STOCH");
        Model.Control interval = cc(arp,"ARP INTERVALS",118,"KNOB",1,0,4,2,blue);
        choices(interval, new int[]{0,21,42,63,84,127},
                "OCT","MAJ","SUS","AUG","MIN","DIM");
        cc(arp,"ARP LENGTH",119,"KNOB",2,0,4,2,blue);

        Model.Control pitch = cc(arp,"PITCH BEND",0,"SLIDER",3,0,4,2,blue);
        pitch.message = "PITCH"; pitch.value = 64;

        String[] notes = {"C4","C#4","D4","D#4","E4","F4","F#4","G4","G#4","A4","A#4","B4"};
        for (int i = 0; i < notes.length; i++) {
            Model.Control note = new Model.Control();
            note.label = notes[i];
            note.kind = "BUTTON"; note.message = "NOTE"; note.number = 60 + i;
            note.color = (i == 1 || i == 3 || i == 6 || i == 8 || i == 10) ? violet : aqua;
            note.x = .017f + i * (.966f / 12f);
            note.y = .54f; note.w = .966f / 12f - .006f; note.h = .37f;
            note.value = 100; note.normalize();
            arp.controls.add(note);
        }
        return project;
    }
}
