package com.domucode.touchmidi;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/** Saved MIDI piano roll; steps are sixteenth notes. */
public final class RollModel {
    public static final class Note {
        public int step, pitch, duration, velocity;
        public Note(int step, int pitch, int duration, int velocity) {
            this.step = step;
            this.pitch = pitch;
            this.duration = duration;
            this.velocity = velocity;
        }
        public JSONObject toJson() {
            JSONObject j = new JSONObject();
            try {
                j.put("step", step);
                j.put("pitch", pitch);
                j.put("duration", duration);
                j.put("velocity", velocity);
            } catch (Exception ignored) {}
            return j;
        }
    }

    public int steps = 64;
    public int bpm = 120;
    public int channel = 1;
    public int velocity = 100;
    public final ArrayList<Note> notes = new ArrayList<>();

    public void resize(int length) {
        steps = Math.max(1, Math.min(1000000, length));
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note n = notes.get(i);
            if (n.step >= steps) notes.remove(i);
            else n.duration = Math.max(1, Math.min(n.duration, steps - n.step));
        }
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        try {
            j.put("steps", steps);
            j.put("bpm", bpm);
            j.put("channel", channel);
            j.put("velocity", velocity);
            JSONArray a = new JSONArray();
            for (Note n : notes) a.put(n.toJson());
            j.put("notes", a);
        } catch (Exception ignored) {}
        return j;
    }

    public static RollModel fromJson(JSONObject j) {
        RollModel roll = new RollModel();
        if (j == null) return roll;
        roll.steps = Math.max(1, Math.min(1000000, j.optInt("steps", 64)));
        roll.bpm = Math.max(20, Math.min(300, j.optInt("bpm", 120)));
        roll.channel = Math.max(1, Math.min(16, j.optInt("channel", 1)));
        roll.velocity = Math.max(1, Math.min(127, j.optInt("velocity", 100)));
        JSONArray a = j.optJSONArray("notes");
        if (a != null) for (int i = 0; i < Math.min(a.length(), 50000); i++) {
            JSONObject n = a.optJSONObject(i);
            if (n == null) continue;
            int step = n.optInt("step", -1);
            int pitch = n.optInt("pitch", -1);
            if (step < 0 || step >= roll.steps || pitch < 0 || pitch > 127) continue;
            int duration = Math.max(1, Math.min(roll.steps-step, n.optInt("duration", 1)));
            int velocity = Math.max(1, Math.min(127, n.optInt("velocity", 100)));
            roll.notes.add(new Note(step, pitch, duration, velocity));
        }
        return roll;
    }
}
