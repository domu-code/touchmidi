package com.domucode.touchmidi;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.ArrayList;

public final class MidiOutput {
    public interface Listener { void onMidiStatusChanged(); }

    public static final class Destination {
        public final MidiDeviceInfo device;
        public final int port;
        public final String name;
        Destination(MidiDeviceInfo d, int p, String n) {
            device = d; port = p; name = n;
        }
        @Override public String toString() { return name + " · port " + (port + 1); }
    }

    private final MidiManager manager;
    private final Listener listener;
    private final SharedPreferences preferences;
    private MidiDevice openedDevice;
    private MidiInputPort openedPort;
    private int activeId = -1;
    private int generation = 0;
    private String selectedName;
    private int selectedPort;
    private String status = "NO MIDI OUTPUT";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MidiOutput(Context context, Listener listener) {
        this.listener = listener;
        this.preferences = context.getSharedPreferences("touchmidi", Context.MODE_PRIVATE);
        this.manager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        this.selectedName = preferences.getString("midiName", "");
        this.selectedPort = preferences.getInt("midiPort", 0);
        if (manager != null) {
            manager.registerDeviceCallback(new MidiManager.DeviceCallback() {
                @Override public void onDeviceAdded(MidiDeviceInfo device) { reconnectSaved(); }
                @Override public void onDeviceRemoved(MidiDeviceInfo device) {
                    if (device.getId() == activeId) {
                        closePort();
                        status = "MIDI DEVICE DISCONNECTED";
                        notifyListener();
                    }
                }
            }, mainHandler);
            reconnectSaved();
        } else {
            status = "ANDROID MIDI NOT AVAILABLE";
        }
    }

    public String getStatus() { return status; }

    private static String deviceName(MidiDeviceInfo info) {
        String n = info.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
        if (n == null || n.trim().isEmpty()) {
            n = "MIDI device #" + info.getId();
        }
        String prefix = info.getType() == MidiDeviceInfo.TYPE_USB ? "USB: "
                : info.getType() == MidiDeviceInfo.TYPE_BLUETOOTH ? "BLE: " : "APP: ";
        return prefix + n;
    }

    public ArrayList<Destination> destinations() {
        ArrayList<Destination> list = new ArrayList<>();
        if (manager == null) return list;
        for (MidiDeviceInfo d : manager.getDevices()) {
            for (int p = 0; p < d.getInputPortCount(); p++)
                list.add(new Destination(d, p, deviceName(d)));
        }
        return list;
    }

    private void reconnectSaved() {
        if (selectedName == null || selectedName.isEmpty() || openedPort != null) return;
        for (Destination d : destinations()) {
            if (d.name.equals(selectedName) && d.port == selectedPort) {
                connect(d);
                return;
            }
        }
        notifyListener();
    }

    public void connect(Destination target) {
        closePort();
        final int ticket = ++generation;
        selectedName = target.name; selectedPort = target.port;
        preferences.edit().putString("midiName", selectedName)
                .putInt("midiPort", selectedPort).apply();
        status = "CONNECTING " + selectedName;
        notifyListener();
        if (manager == null) return;
        manager.openDevice(target.device, device -> {
            if (ticket != generation) {
                if (device != null) try { device.close(); } catch (IOException ignored) {}
                return;
            }
            if (device == null) {
                status = "UNABLE TO OPEN MIDI DEVICE";
                notifyListener();
                return;
            }
            MidiInputPort input = device.openInputPort(target.port);
            if (input == null) {
                try { device.close(); } catch (IOException ignored) {}
                status = "MIDI PORT IS BUSY";
            } else {
                openedDevice = device;
                openedPort = input;
                activeId = target.device.getId();
                status = "CONNECTED · " + target.name + " · " + (target.port + 1);
            }
            notifyListener();
        }, mainHandler);
    }

    public void disconnect() {
        generation++;
        closePort();
        selectedName = "";
        preferences.edit().remove("midiName").remove("midiPort").apply();
        status = "NO MIDI OUTPUT";
        notifyListener();
    }

    private void closePort() {
        try { if (openedPort != null) openedPort.close(); } catch (IOException ignored) {}
        try { if (openedDevice != null) openedDevice.close(); } catch (IOException ignored) {}
        openedPort = null; openedDevice = null; activeId = -1;
    }

    private void notifyListener() { if (listener != null) listener.onMidiStatusChanged(); }

    private void bytes(int... values) {
        if (openedPort == null) return;
        byte[] data = new byte[values.length];
        for (int i = 0; i < values.length; i++) data[i] = (byte)values[i];
        try { openedPort.send(data, 0, data.length); }
        catch (IOException e) {
            closePort(); status = "MIDI SEND ERROR: " + e.getMessage();
            notifyListener();
        }
    }

    public void noteOn(int channel, int pitch, int velocity) {
        int ch = Math.max(0, Math.min(15, channel - 1));
        bytes(0x90 | ch, Math.max(0, Math.min(127, pitch)),
                Math.max(1, Math.min(127, velocity)));
    }

    public void noteOff(int channel, int pitch) {
        int ch = Math.max(0, Math.min(15, channel - 1));
        bytes(0x80 | ch, Math.max(0, Math.min(127, pitch)), 0);
    }

    public void send(Model.Control c, int value) {
        int channel = Math.max(0, Math.min(15, c.channel - 1));
        int v = Math.max(0, Math.min(127, value));
        switch (c.message) {
            case "NOTE":
                bytes(0x90 | channel, c.number, Math.max(1, v));
                break;
            case "PROGRAM":
                bytes(0xc0 | channel, c.number);
                break;
            case "PITCH":
                int bend = Math.round(v * 16383f / 127f);
                bytes(0xe0 | channel, bend & 127, bend >> 7);
                break;
            case "PRESSURE":
                bytes(0xd0 | channel, v);
                break;
            case "POLY PRESS":
                bytes(0xa0 | channel, c.number, v);
                break;
            default:
                bytes(0xb0 | channel, c.number, v);
                break;
        }
    }

    public void sendSecondary(Model.Control c, int value) {
        int channel = Math.max(0, Math.min(15, c.channel - 1));
        bytes(0xb0 | channel, c.second, Math.max(0, Math.min(127, value)));
    }

    public void release(Model.Control c) {
        int ch = Math.max(0, Math.min(15, c.channel - 1));
        if ("NOTE".equals(c.message)) bytes(0x80 | ch, c.number, 0);
        else if ("CC".equals(c.message)) bytes(0xb0 | ch, c.number, c.min);
        else if ("PRESSURE".equals(c.message)) bytes(0xd0 | ch, 0);
        else if ("POLY PRESS".equals(c.message)) bytes(0xa0 | ch, c.number, 0);
        else if ("PITCH".equals(c.message)) bytes(0xe0 | ch, 0, 64);
    }
}
