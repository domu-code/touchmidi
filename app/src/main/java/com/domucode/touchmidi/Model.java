package com.domucode.touchmidi;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.UUID;

public final class Model {
    private Model() {}

    public static final class Control {
        public String id = UUID.randomUUID().toString();
        public String label = "CONTROL";
        public String kind = "KNOB";
        public String message = "CC";
        public int channel = 1;
        public int number = 1;
        public int second = 2;
        public int min = 0;
        public int max = 127;
        public int value = 64;
        public int value2 = 64;
        public int color = 0xff49d8c7;
        public float x = .1f, y = .1f, w = .17f, h = .27f;
        public boolean state = false;
        public int[] choices = {};
        public String[] choiceNames = {};

        public int bound(int v) { return Math.max(0, Math.min(127, v)); }

        public int fromFraction(float fraction) {
            float t = Math.max(0, Math.min(1, fraction));
            if (choices.length > 0) {
                int ix = Math.min(choices.length - 1, Math.round(t * (choices.length - 1)));
                return bound(choices[ix]);
            }
            int low = Math.min(min, max), high = Math.max(min, max);
            return bound(Math.round(low + (high - low) * t));
        }

        public float fraction() {
            if (choices.length > 0) {
                int best = 0;
                for (int i = 1; i < choices.length; i++) {
                    if (Math.abs(choices[i] - value) < Math.abs(choices[best] - value)) best = i;
                }
                return choices.length == 1 ? 0f : (float) best / (choices.length - 1);
            }
            return (value - Math.min(min, max)) / (float) Math.max(1, Math.abs(max - min));
        }

        public String displayValue() {
            if (choices.length > 0 && choiceNames.length == choices.length) {
                int closest = 0;
                for (int i = 1; i < choices.length; i++)
                    if (Math.abs(choices[i] - value) < Math.abs(choices[closest] - value)) closest = i;
                return choiceNames[closest];
            }
            return Integer.toString(value);
        }

        public void normalize() {
            w = Math.max(.055f, Math.min(1, w));
            h = Math.max(.09f, Math.min(1, h));
            x = Math.max(0, Math.min(1 - w, x));
            y = Math.max(0, Math.min(1 - h, y));
            channel = Math.max(1, Math.min(16, channel));
            number = bound(number); second = bound(second);
            min = bound(min); max = bound(max);
            value = bound(value); value2 = bound(value2);
        }

        public JSONObject toJson() {
            JSONObject o = new JSONObject();
            try {
                o.put("id", id); o.put("label", label); o.put("kind", kind);
                o.put("message", message); o.put("channel", channel);
                o.put("number", number); o.put("second", second);
                o.put("min", min); o.put("max", max); o.put("value", value);
                o.put("value2", value2); o.put("color", color);
                o.put("x", (double)x); o.put("y", (double)y);
                o.put("w", (double)w); o.put("h", (double)h);
                o.put("state", state);
                JSONArray v = new JSONArray(); for (int c : choices) v.put(c);
                JSONArray names = new JSONArray(); for (String s : choiceNames) names.put(s);
                o.put("choices", v); o.put("choiceNames", names);
            } catch (Exception ignored) {}
            return o;
        }

        public static Control fromJson(JSONObject o) {
            Control c = new Control();
            c.id = o.optString("id", c.id);
            c.label = o.optString("label", c.label);
            c.kind = o.optString("kind", c.kind);
            c.message = o.optString("message", c.message);
            c.channel = o.optInt("channel", 1);
            c.number = o.optInt("number", 1);
            c.second = o.optInt("second", 2);
            c.min = o.optInt("min", 0); c.max = o.optInt("max", 127);
            c.value = o.optInt("value", 64); c.value2 = o.optInt("value2", 64);
            c.color = o.optInt("color", 0xff49d8c7);
            c.x = (float)o.optDouble("x", .1);
            c.y = (float)o.optDouble("y", .1);
            c.w = (float)o.optDouble("w", .17);
            c.h = (float)o.optDouble("h", .27);
            c.state = o.optBoolean("state", false);
            JSONArray arr = o.optJSONArray("choices");
            if (arr != null) {
                c.choices = new int[arr.length()];
                for (int i = 0; i < arr.length(); i++) c.choices[i] = c.bound(arr.optInt(i));
            }
            JSONArray names = o.optJSONArray("choiceNames");
            if (names != null) {
                c.choiceNames = new String[names.length()];
                for (int i = 0; i < names.length(); i++) c.choiceNames[i] = names.optString(i, "");
            }
            c.normalize();
            return c;
        }
    }

    public static final class Page {
        public String name;
        public final ArrayList<Control> controls = new ArrayList<>();
        public Page(String name) { this.name = name; }

        public JSONObject toJson() {
            JSONObject o = new JSONObject();
            try {
                o.put("name", name);
                JSONArray arr = new JSONArray();
                for (Control c : controls) arr.put(c.toJson());
                o.put("controls", arr);
            } catch (Exception ignored) {}
            return o;
        }

        public static Page fromJson(JSONObject o) {
            Page p = new Page(o.optString("name", "PAGE"));
            JSONArray arr = o.optJSONArray("controls");
            if (arr != null) for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj != null) p.controls.add(Control.fromJson(obj));
            }
            return p;
        }
    }

    public static final class Project {
        public String title = "UNTITLED";
        public int currentPage = 0;
        public final ArrayList<Page> pages = new ArrayList<>();

        public Page page() {
            if (pages.isEmpty()) pages.add(new Page("PAGE 1"));
            currentPage = Math.max(0, Math.min(currentPage, pages.size() - 1));
            return pages.get(currentPage);
        }

        public JSONObject toJson() {
            JSONObject o = new JSONObject();
            try {
                o.put("version", 1);
                o.put("title", title); o.put("currentPage", currentPage);
                JSONArray arr = new JSONArray();
                for (Page p : pages) arr.put(p.toJson());
                o.put("pages", arr);
            } catch (Exception ignored) {}
            return o;
        }

        public static Project fromJson(String data) throws Exception {
            JSONObject root = new JSONObject(data);
            Project p = new Project();
            p.title = root.optString("title", "UNTITLED");
            JSONArray arr = root.optJSONArray("pages");
            if (arr == null || arr.length() == 0 || arr.length() > 100)
                throw new IllegalArgumentException("Layout contains no pages or too many pages");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) p.pages.add(Page.fromJson(o));
            }
            if (p.pages.isEmpty()) throw new IllegalArgumentException("No valid pages");
            p.currentPage = root.optInt("currentPage", 0);
            p.page();
            return p;
        }
    }
}
