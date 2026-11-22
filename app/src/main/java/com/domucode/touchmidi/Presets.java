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
    private static void grid(Model.Page page, String[] labels, int[] numbers, String[] kinds, int colour) {
        int cols = 4;
        int rows = Math.max(1, (int)Math.ceil(labels.length / (float)cols));
        for (int i=0; i<labels.length; i++) {
            String kind = kinds == null ? "KNOB" : kinds[i];
            Model.Control c = cc(page, labels[i], numbers[i], kind, i%cols, i/cols, cols, rows, colour);
            if ("TOGGLE".equals(kind)) c.value=0;
        }
    }

    private static void keys(Model.Page page) {
        String[] names={"C4","C#4","D4","D#4","E4","F4","F#4","G4","G#4","A4","A#4","B4"};
        for(int i=0;i<12;i++) {
            Model.Control c=new Model.Control();
            c.label=names[i];
            c.kind="BUTTON";
            c.message="NOTE";
            c.number=60+i;
            c.value=105;
            c.color=(i==1||i==3||i==6||i==8||i==10)?0xffbb9afa:0xff49d8c7;
            c.x=.018f+i*.0803f;
            c.y=.43f;c.w=.074f;c.h=.50f;
            c.normalize();
            page.controls.add(c);
        }
    }

    /**
     * LOOM M7.7.0 references real parameter names from the original source.
     * It uses MIDI learn, so these CC assignments are a suggested learn map,
     * not factory-standard numbers.
     */
    public static Model.Project loom() {
        Model.Project project=new Model.Project();
        project.title="LOOM M7.7 · MIDI LEARN";

        Model.Page voices=new Model.Page("PL / BRAIDS");
        project.pages.add(voices);
        grid(voices,new String[]{
                "PL HARMONICS","PL TIMBRE","PL MORPH","PL LEVEL",
                "BR HARMONICS","BR TIMBRE","BR MORPH","BR LEVEL",
                "PL DECAY","PL COLOUR","BR DECAY","BR COLOUR"
            },new int[]{20,21,22,23,24,25,26,27,28,29,30,31},null,0xff49d8c7);

        Model.Page filter=new Model.Page("FILTER / MIX");
        project.pages.add(filter);
        grid(filter,new String[]{
                "CUTOFF","RESONANCE","FILTER MODE","FILTER MORPH",
                "FILTER SLOPE","FILTER WET","PL PAN","BR PAN",
                "DMG LEVEL","SAMPLE LEVEL","RINGS LEVEL","MASTER"
            },new int[]{32,33,34,35,36,37,38,39,40,41,42,43},null,0xffffb56e);

        Model.Page dmg=new Model.Page("DMG / SAMPLE");
        project.pages.add(dmg);
        grid(dmg,new String[]{
                "DMG WAVE SCAN","DMG WIDTH","DMG JITTER","DMG FOLD",
                "DMG FILTER CUT","DMG FILTER RES","DMG PAN","SAMPLE PAN",
                "GRAIN SIZE","GRAIN DENSITY","SAMPLE FINE","DMG WRAP"
            },new int[]{44,45,46,47,48,49,50,51,52,53,54,55},null,0xff71b8ff);

        Model.Page fx=new Model.Page("FX / BEADS");
        project.pages.add(fx);
        grid(fx,new String[]{
                "DRIVE MIX","CHORUS RATE","CHORUS DEPTH","CHORUS MIX",
                "DELAY TIME","DELAY FEEDBACK","DELAY MIX","BEADS TIME",
                "BEADS SIZE","BEADS DENSITY","BEADS MIX","REVERB MIX"
            },new int[]{56,57,58,59,60,61,62,63,65,66,67,68},null,0xffbb9afa);

        Model.Page mod=new Model.Page("TIDES / VOICES");
        project.pages.add(mod);
        grid(mod,new String[]{
                "TIDES 1 RATE","TIDES 1 SHAPE","TIDES 1 SLOPE","TIDES 1 SMOOTH",
                "TIDES 2 RATE","TIDES 2 SHAPE","TIDES 2 SLOPE","TIDES 2 SMOOTH",
                "RINGS STRUCT","RINGS BRIGHT","RINGS DAMPING","AIR BREATH"
            },new int[]{69,70,71,72,73,74,75,76,77,78,79,80},null,0xffa4dd77);

        Model.Page performance=new Model.Page("PERFORMANCE");
        project.pages.add(performance);
        Model.Control wheel=cc(performance,"MOD WHEEL",1,"FADER",0,0,4,3,0xff71b8ff);
        wheel.value=0;
        Model.Control pitch=cc(performance,"PITCH BEND",0,"SLIDER",1,0,4,3,0xffffb56e);
        pitch.message="PITCH";pitch.value=64;
        Model.Control hold=cc(performance,"SUSTAIN",64,"TOGGLE",2,0,4,3,0xff49d8c7);
        hold.value=0;
        keys(performance);
        return project;
    }

    /**
     * MetroGaia 6.9 responds to CC20-35 (motion range sliders),
     * CC60-63 (four gates), and CC1 (mod wheel).
     * CC44-51 are its configured outgoing XY motion assignments.
     */
    public static Model.Project metroGaia() {
        Model.Project project=new Model.Project();
        project.title="METROGAIA V6.9";

        Model.Page rangesA=new Model.Page("RANGES 1–4");
        project.pages.add(rangesA);
        grid(rangesA,new String[]{
                "CH1 X MIN","CH1 X MAX","CH1 Y MIN","CH1 Y MAX",
                "CH2 X MIN","CH2 X MAX","CH2 Y MIN","CH2 Y MAX"
            },new int[]{20,21,22,23,24,25,26,27},null,0xff49d8c7);

        Model.Page rangesB=new Model.Page("RANGES 5–8");
        project.pages.add(rangesB);
        grid(rangesB,new String[]{
                "CH3 X MIN","CH3 X MAX","CH3 Y MIN","CH3 Y MAX",
                "CH4 X MIN","CH4 X MAX","CH4 Y MIN","CH4 Y MAX"
            },new int[]{28,29,30,31,32,33,34,35},null,0xffffb56e);

        Model.Page xy=new Model.Page("MOTION XY");
        project.pages.add(xy);
        for(int i=0;i<4;i++) {
            Model.Control c=cc(xy,"MOTION "+(i+1)+" XY",44+i*2,"XY",
                    i%2,i/2,2,2,0xffbb9afa);
            c.second=45+i*2;
            c.value=64;c.value2=64;
        }

        Model.Page gates=new Model.Page("GATES / KEYS");
        project.pages.add(gates);
        for(int i=0;i<4;i++) {
            Model.Control c=cc(gates,"GATE "+(i+1),60+i,"TOGGLE",i,0,4,3,0xffa4dd77);
            c.value=0;
        }
        keys(gates);

        Model.Page expression=new Model.Page("EXPRESSION");
        project.pages.add(expression);
        Model.Control wheel=cc(expression,"MOD WHEEL",1,"FADER",0,0,3,1,0xff71b8ff);
        wheel.value=0;
        Model.Control pitch=cc(expression,"PITCH BEND",0,"SLIDER",1,0,3,1,0xffbb9afa);
        pitch.message="PITCH";pitch.value=64;
        Model.Control pressure=cc(expression,"CHANNEL PRESSURE",0,"FADER",2,0,3,1,0xffffb56e);
        pressure.message="PRESSURE";pressure.value=0;
        return project;
    }

}
