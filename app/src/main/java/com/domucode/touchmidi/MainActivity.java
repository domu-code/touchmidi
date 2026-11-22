package com.domucode.touchmidi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public final class MainActivity extends Activity {
    private static final int DARK = 0xff0b1118;
    private static final int PANEL = 0xff14212b;
    private static final int WHITE = 0xffedf3f5;
    private static final int MUTED = 0xff9fb3bd;
    private static final int AQUA = 0xff49d8c7;
    private static final String[] TYPES = {"KNOB","FADER","SLIDER","BUTTON","TOGGLE","XY"};
    private static final String[] MESSAGES = {"CC","NOTE","PROGRAM","PITCH","PRESSURE","POLY PRESS"};
    private static final String[] COLORS = {"AQUA","ORANGE","VIOLET","BLUE","LIME","PINK","WHITE"};
    private static final int[] COLOR_VALUES = {
            0xff49d8c7,0xffffb56e,0xffbb9afa,0xff71b8ff,0xffa4dd77,0xffff86b1,0xffe2eaf0
    };
    private static final int REQ_IMPORT=801, REQ_EXPORT=802;
    private Model.Project layout;
    private MidiOutput midi;
    private ControllerView controller;
    private boolean editMode = false;
    private boolean snap = false;
    private LinearLayout pageTabs;
    private TextView titleText, midiText;
    private Button modeButton, inspectButton, duplicateButton, removeButton;
    private int newCount=0;

    public Model.Project project() { return layout; }
    public MidiOutput midi() { return midi; }
    public boolean isEditing() { return editMode; }
    public boolean snapEnabled() { return snap; }

    private int dp(float val) { return (int)(getResources().getDisplayMetrics().density*val+.5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        snap = getPreferences(MODE_PRIVATE).getBoolean("snap",false);
        String saved = getPreferences(MODE_PRIVATE).getString("layout",null);
        if(saved!=null) {
            try { layout=Model.Project.fromJson(saved); }
            catch (Exception e) { layout=Presets.nts1(); }
        } else layout=Presets.nts1();
        midi = new MidiOutput(this, this::updateMidiStatus);
        buildUI();
        updateMidiStatus();
    }

    private GradientDrawable background(int color,int stroke) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);d.setCornerRadius(dp(8));
        if(stroke!=0)d.setStroke(dp(1),stroke);
        return d;
    }

    private TextView text(String title,int size,int color) {
        TextView t=new TextView(this);
        t.setText(title); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(android.view.Gravity.CENTER_VERTICAL);
        return t;
    }

    private Button button(String label,Runnable onClick) {
        Button b=new Button(this);
        b.setText(label); b.setTextSize(12);b.setAllCaps(false);
        b.setPadding(dp(11),0,dp(11),0);
        b.setTextColor(WHITE);b.setBackground(background(PANEL,0xff324957));
        b.setOnClickListener(v->onClick.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,dp(39));
        lp.setMargins(dp(3),dp(3),dp(3),dp(3));
        b.setLayoutParams(lp);
        return b;
    }

    private void buildUI() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(DARK);
        root.setPadding(dp(6),dp(4),dp(6),dp(3));

        LinearLayout header=new LinearLayout(this);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        titleText=text("TOUCHMIDI",16,WHITE);
        titleText.setTypeface(null,android.graphics.Typeface.BOLD);
        titleText.setSingleLine(true);titleText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams titleP=new LinearLayout.LayoutParams(0,dp(42),1);
        titleText.setLayoutParams(titleP);header.addView(titleText);
        header.addView(button("MIDI",this::showMidiDevices));
        modeButton=button("EDIT",this::toggleMode);header.addView(modeButton);
        root.addView(header);

        HorizontalScrollView actionsScroll=new HorizontalScrollView(this);
        actionsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout actions=new LinearLayout(this); actions.setGravity(android.view.Gravity.CENTER_VERTICAL);
        actions.addView(button("+ ADD",this::showAddDialog));
        inspectButton=button("INSPECT",this::showInspector);actions.addView(inspectButton);
        duplicateButton=button("COPY",this::duplicateSelected);actions.addView(duplicateButton);
        removeButton=button("DELETE",this::deleteSelected);actions.addView(removeButton);
        actions.addView(button("PAGES",this::pageOptions));
        actions.addView(button("PROJECT",this::projectOptions));
        actionsScroll.addView(actions);root.addView(actionsScroll);

        HorizontalScrollView pagesScroll=new HorizontalScrollView(this);
        pagesScroll.setHorizontalScrollBarEnabled(false);
        pageTabs=new LinearLayout(this);
        pagesScroll.addView(pageTabs);root.addView(pagesScroll);

        controller=new ControllerView(this);
        root.addView(controller,new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,0,1));

        midiText=text("NO MIDI OUTPUT",11,MUTED);
        midiText.setSingleLine(true);
        midiText.setPadding(dp(7),0,dp(7),0);
        root.addView(midiText,new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,dp(23)));
        setContentView(root);
        refreshAll();
    }

    private void refreshAll() {
        titleText.setText("TOUCHMIDI  /  "+layout.title);
        modeButton.setText(editMode?"PLAY":"EDIT");
        onSelectionChanged();
        rebuildPages();
        controller.invalidate();
    }

    public void onSelectionChanged() {
        boolean has = editMode && controller!=null && controller.getSelected()!=null;
        if(inspectButton!=null) inspectButton.setEnabled(has);
        if(duplicateButton!=null) duplicateButton.setEnabled(has);
        if(removeButton!=null) removeButton.setEnabled(has);
    }

    public void saveLayout() {
        getPreferences(MODE_PRIVATE).edit().putString("layout",layout.toJson().toString()).apply();
    }

    public void updateMidiStatus() {
        if(midiText!=null && midi!=null) midiText.setText(midi.getStatus());
    }

    private void toggleMode() {
        controller.clearPointers();editMode=!editMode;
        if(!editMode) controller.select(null);
        refreshAll();
        Toast.makeText(this,editMode?"EDIT: drag to move; drag white corner to resize":"PLAY: controls locked, MIDI live",Toast.LENGTH_SHORT).show();
    }

    private void showAddDialog() {
        if(!editMode) { editMode=true;refreshAll(); }
        new AlertDialog.Builder(this).setTitle("ADD A CONTROL")
                .setItems(new String[]{"Rotary knob","Vertical fader","Horizontal slider","Momentary button","Toggle switch","XY pad"},
                        (d,index)->addControl(TYPES[index])).show();
    }

    private void addControl(String kind) {
        Model.Control c=new Model.Control();
        c.kind=kind;c.label=kind.equals("FADER")?"FADER":kind;
        c.number=1;c.value=64;
        switch(kind) {
            case "FADER": c.w=.13f;c.h=.34f;break;
            case "SLIDER": c.w=.27f;c.h=.19f;break;
            case "BUTTON": case "TOGGLE": c.w=.18f;c.h=.19f;c.value=100;break;
            case "XY": c.w=.29f;c.h=.33f;break;
            default: c.w=.17f;c.h=.28f;
        }
        c.x=.07f+newCount%7*.075f;
        c.y=.08f+(newCount/7%5)*.09f;
        newCount++;c.normalize();
        layout.page().controls.add(c);
        controller.select(c);onSelectionChanged();
        saveLayout();controller.invalidate();
        editControl(c);
    }

    private static int indexOf(String[] options,String value) {
        for(int i=0;i<options.length;i++)if(options[i].equals(value))return i;
        return 0;
    }

    private Spinner spinner(LinearLayout parent,String label,String[] options,int selected) {
        parent.addView(fieldLabel(label));
        Spinner s=new Spinner(this);
        ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);s.setSelection(Math.max(0,Math.min(options.length-1,selected)));
        parent.addView(s);
        return s;
    }

    private TextView fieldLabel(String label) {
        TextView t=text(label,12,MUTED);
        t.setPadding(0,dp(9),0,dp(2));
        return t;
    }

    private EditText field(LinearLayout parent,String title,String value,boolean decimal) {
        parent.addView(fieldLabel(title));
        EditText e=new EditText(this);
        e.setText(value);e.setTextColor(WHITE);
        e.setTextSize(15);e.setSingleLine(true);e.setSelectAllOnFocus(true);
        if(decimal)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL|
                android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        parent.addView(e);
        return e;
    }

    private int readInt(EditText e,int fallback) {
        try{return Integer.parseInt(e.getText().toString().trim());}
        catch(Exception ignored){return fallback;}
    }

    private float readFloat(EditText e,float fallback) {
        try{return Float.parseFloat(e.getText().toString().trim());}
        catch(Exception ignored){return fallback;}
    }

    private String csv(int[] numbers) {
        StringBuilder b=new StringBuilder();
        for(int i=0;i<numbers.length;i++) {
            if(i>0)b.append(", ");b.append(numbers[i]);
        }
        return b.toString();
    }

    private String csvStrings(String[] values) {
        StringBuilder b=new StringBuilder();
        for(int i=0;i<values.length;i++) {
            if(i>0)b.append(", ");b.append(values[i]);
        }
        return b.toString();
    }

    private void showInspector() {
        Model.Control c=controller.getSelected();
        if(c!=null)editControl(c);
        else Toast.makeText(this,"Select a control in EDIT mode",Toast.LENGTH_SHORT).show();
    }

    private void editControl(Model.Control c) {
        ScrollView scroll=new ScrollView(this);
        LinearLayout form=new LinearLayout(this);
        form.setPadding(dp(22),dp(8),dp(22),dp(18));
        form.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(form);

        EditText label=field(form,"LABEL",c.label,false);
        Spinner kind=spinner(form,"CONTROL TYPE",TYPES,indexOf(TYPES,c.kind));
        Spinner msg=spinner(form,"MIDI MESSAGE",MESSAGES,indexOf(MESSAGES,c.message));
        EditText channel=field(form,"MIDI CHANNEL (1-16)",Integer.toString(c.channel),true);
        EditText number=field(form,"CC / NOTE / PROGRAM NUMBER (0-127)",Integer.toString(c.number),true);
        EditText second=field(form,"SECOND CC (XY Y-AXIS)",Integer.toString(c.second),true);
        EditText initial=field(form,"CURRENT VALUE (0-127)",Integer.toString(c.value),true);
        EditText min=field(form,"MINIMUM VALUE (0-127)",Integer.toString(c.min),true);
        EditText max=field(form,"MAXIMUM VALUE (0-127)",Integer.toString(c.max),true);
        EditText x=field(form,"LEFT POSITION %",String.format(Locale.US,"%.1f",c.x*100),true);
        EditText y=field(form,"TOP POSITION %",String.format(Locale.US,"%.1f",c.y*100),true);
        EditText w=field(form,"WIDTH %",String.format(Locale.US,"%.1f",c.w*100),true);
        EditText h=field(form,"HEIGHT %",String.format(Locale.US,"%.1f",c.h*100),true);
        int ci=0;for(int i=0;i<COLOR_VALUES.length;i++)if(COLOR_VALUES[i]==c.color)ci=i;
        Spinner color=spinner(form,"ACCENT COLOUR",COLORS,ci);
        EditText values=field(form,"STEPPED CC VALUES (OPTIONAL, COMMA-SEPARATED)",csv(c.choices),false);
        EditText names=field(form,"STEP LABELS (OPTIONAL, COMMA-SEPARATED)",csvStrings(c.choiceNames),false);
        new AlertDialog.Builder(this).setTitle("EDIT CONTROL")
                .setView(scroll).setNegativeButton("CANCEL",null)
                .setPositiveButton("APPLY",(dialog,which)->{
                    c.label=label.getText().toString().trim();
                    if(c.label.isEmpty())c.label="CONTROL";
                    c.kind=TYPES[kind.getSelectedItemPosition()];
                    c.message=MESSAGES[msg.getSelectedItemPosition()];
                    c.channel=readInt(channel,c.channel);
                    c.number=readInt(number,c.number);
                    c.second=readInt(second,c.second);
                    c.value=readInt(initial,c.value);
                    c.min=readInt(min,c.min);c.max=readInt(max,c.max);
                    c.x=readFloat(x,c.x*100)/100f;
                    c.y=readFloat(y,c.y*100)/100f;
                    c.w=readFloat(w,c.w*100)/100f;
                    c.h=readFloat(h,c.h*100)/100f;
                    c.color=COLOR_VALUES[color.getSelectedItemPosition()];
                    String csv=values.getText().toString().trim();
                    if(csv.isEmpty()) c.choices=new int[0];
                    else {
                        String[] parts=csv.split(",");
                        ArrayList<Integer> parsed=new ArrayList<>();
                        for(String part:parts) {
                            try{parsed.add(Math.max(0,Math.min(127,Integer.parseInt(part.trim()))));}
                            catch(NumberFormatException ignored){}
                        }
                        c.choices=new int[parsed.size()];
                        for(int i=0;i<parsed.size();i++)c.choices[i]=parsed.get(i);
                    }
                    String nameCsv=names.getText().toString().trim();
                    c.choiceNames=nameCsv.isEmpty()?new String[0]:nameCsv.split("\\s*,\\s*");
                    if(c.choiceNames.length!=c.choices.length)c.choiceNames=new String[0];
                    c.normalize();saveLayout();controller.invalidate();
                }).show();
    }

    private void duplicateSelected() {
        Model.Control original=controller.getSelected();
        if(original==null)return;
        Model.Control copy=Model.Control.fromJson(original.toJson());
        copy.id=UUID.randomUUID().toString();
        copy.x+=.035f;copy.y+=.04f;copy.normalize();
        layout.page().controls.add(copy);
        controller.select(copy);saveLayout();controller.invalidate();
    }

    private void deleteSelected() {
        Model.Control c=controller.getSelected();if(c==null)return;
        new AlertDialog.Builder(this).setTitle("DELETE "+c.label+"?")
                .setMessage("Remove this control from the current page?")
                .setNegativeButton("CANCEL",null)
                .setPositiveButton("DELETE",(d,w)->{
                    layout.page().controls.remove(c);controller.select(null);
                    onSelectionChanged();saveLayout();controller.invalidate();
                }).show();
    }

    private void rebuildPages() {
        pageTabs.removeAllViews();
        for(int i=0;i<layout.pages.size();i++) {
            final int pageIndex=i;
            Button b=button(layout.pages.get(i).name,()->switchPage(pageIndex));
            if(i==layout.currentPage)b.setBackground(background(0xff215b60,AQUA));
            pageTabs.addView(b);
        }
        pageTabs.addView(button("+ PAGE",this::addPage));
    }

    private void switchPage(int index) {
        controller.clearPointers();
        layout.currentPage=index;controller.select(null);
        onSelectionChanged();saveLayout();rebuildPages();controller.invalidate();
    }

    private void addPage() {
        if(layout.pages.size()>=100) {
            Toast.makeText(this,"Maximum 100 pages",Toast.LENGTH_SHORT).show();return;
        }
        Model.Page page=new Model.Page("PAGE "+(layout.pages.size()+1));
        layout.pages.add(page);switchPage(layout.pages.size()-1);
    }

    private void pageOptions() {
        new AlertDialog.Builder(this).setTitle("MANAGE PAGES")
                .setItems(new String[]{"New page","Rename current page","Duplicate current page","Delete current page"},
                        (d,which)->{
                            if(which==0)addPage();
                            if(which==1)renamePage();
                            if(which==2)duplicatePage();
                            if(which==3)deletePage();
                        }).show();
    }

    private void renamePage() {
        EditText edit=new EditText(this);
        edit.setSingleLine(true);edit.setText(layout.page().name);edit.selectAll();
        new AlertDialog.Builder(this).setTitle("PAGE NAME").setView(edit)
                .setNegativeButton("CANCEL",null)
                .setPositiveButton("SAVE",(d,w)->{
                    String name=edit.getText().toString().trim();
                    if(!name.isEmpty())layout.page().name=name;
                    saveLayout();rebuildPages();
                }).show();
    }

    private void duplicatePage() {
        if(layout.pages.size()>=100)return;
        Model.Page original=layout.page();
        Model.Page copy=Model.Page.fromJson(original.toJson());
        copy.name=original.name+" COPY";
        for(Model.Control c:copy.controls)c.id=UUID.randomUUID().toString();
        layout.pages.add(layout.currentPage+1,copy);
        switchPage(layout.currentPage+1);
    }

    private void deletePage() {
        if(layout.pages.size()==1) {
            Toast.makeText(this,"Keep at least one page",Toast.LENGTH_SHORT).show();return;
        }
        new AlertDialog.Builder(this).setTitle("DELETE PAGE?")
                .setMessage("All controls on this page will be removed.")
                .setNegativeButton("CANCEL",null)
                .setPositiveButton("DELETE",(d,w)->{
                    layout.pages.remove(layout.currentPage);
                    switchPage(Math.min(layout.currentPage,layout.pages.size()-1));
                }).show();
    }

    private void projectOptions() {
        new AlertDialog.Builder(this).setTitle("PROJECT / PRESETS")
                .setItems(new String[]{"New empty project","Load KORG NTS-1 MK1","Rename project",
                        "Export layout JSON","Import layout JSON",
                        "Snap to grid: "+(snap?"ON":"OFF"),
                        "All notes off (MIDI panic)","Help / MIDI setup"},
                        (d,which)->{
                            switch(which) {
                                case 0:confirmReplace(false);break;
                                case 1:confirmReplace(true);break;
                                case 2:renameProject();break;
                                case 3:exportLayout();break;
                                case 4:importLayout();break;
                                case 5:
                                    snap=!snap;getPreferences(MODE_PRIVATE).edit().putBoolean("snap",snap).apply();
                                    Toast.makeText(this,"Grid snapping "+(snap?"enabled":"disabled"),Toast.LENGTH_SHORT).show();
                                    break;
                                case 6:panic();break;
                                case 7:showHelp();break;
                            }
                        }).show();
    }

    private void confirmReplace(boolean nts) {
        new AlertDialog.Builder(this).setTitle("REPLACE PROJECT?")
                .setMessage("This will replace your current on-device layout. Export your project first to keep a copy.")
                .setNegativeButton("CANCEL",null)
                .setPositiveButton("REPLACE",(d,w)->{
                    controller.clearPointers();
                    layout=nts?Presets.nts1():Presets.empty();
                    controller.select(null);saveLayout();refreshAll();
                }).show();
    }

    private void renameProject() {
        EditText e=new EditText(this);e.setSingleLine(true);
        e.setText(layout.title);e.selectAll();
        new AlertDialog.Builder(this).setTitle("PROJECT NAME").setView(e)
                .setNegativeButton("CANCEL",null)
                .setPositiveButton("SAVE",(d,w)->{
                    String n=e.getText().toString().trim();
                    if(!n.isEmpty())layout.title=n;
                    saveLayout();refreshAll();
                }).show();
    }

    private void exportLayout() {
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        String file=layout.title.toLowerCase(Locale.US).replaceAll("[^a-z0-9_-]+","_")+".json";
        intent.putExtra(Intent.EXTRA_TITLE,file);
        startActivityForResult(intent,REQ_EXPORT);
    }

    private void importLayout() {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent,REQ_IMPORT);
    }

    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK || data==null || data.getData()==null)return;
        Uri uri=data.getData();
        try {
            if(request==REQ_EXPORT) {
                try(OutputStream out=getContentResolver().openOutputStream(uri)) {
                    if(out==null)throw new Exception("Unable to open output");
                    out.write(layout.toJson().toString(2).getBytes(StandardCharsets.UTF_8));
                }
                Toast.makeText(this,"Layout exported",Toast.LENGTH_SHORT).show();
            } else if(request==REQ_IMPORT) {
                ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                try(InputStream input=getContentResolver().openInputStream(uri)) {
                    if(input==null)throw new Exception("Unable to read file");
                    byte[] block=new byte[8192];int count;
                    while((count=input.read(block))!=-1) {
                        buffer.write(block,0,count);
                        if(buffer.size()>2_000_000)throw new Exception("Layout file too large");
                    }
                }
                Model.Project imported=Model.Project.fromJson(buffer.toString("UTF-8"));
                controller.clearPointers();layout=imported;controller.select(null);
                saveLayout();refreshAll();
                Toast.makeText(this,"Layout imported",Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            new AlertDialog.Builder(this).setTitle("FILE ERROR")
                    .setMessage(e.getMessage()).setPositiveButton("OK",null).show();
        }
    }

    private void showMidiDevices() {
        ArrayList<MidiOutput.Destination> destinations=midi.destinations();
        String[] names=new String[destinations.size()+1];
        names[0]="DISCONNECT OUTPUT";
        for(int i=0;i<destinations.size();i++)names[i+1]=destinations.get(i).toString();
        new AlertDialog.Builder(this).setTitle("CHOOSE MIDI OUTPUT")
                .setMessage(destinations.isEmpty() ?
                        "No Android MIDI destinations detected. Attach a class-compliant USB MIDI device using USB OTG, or enable a virtual/Android-exposed BLE MIDI port." : null)
                .setItems(names,(d,which)->{
                    if(which==0)midi.disconnect();
                    else midi.connect(destinations.get(which-1));
                }).setNegativeButton("CANCEL",null).show();
    }

    private void panic() {
        for(int ch=1;ch<=16;ch++) {
            Model.Control c=new Model.Control();
            c.message="CC";c.channel=ch;
            c.number=123;midi.send(c,0);
            c.number=120;midi.send(c,0);
        }
        Toast.makeText(this,"All notes off sent on 16 channels",Toast.LENGTH_SHORT).show();
    }

    private void showHelp() {
        new AlertDialog.Builder(this).setTitle("TOUCHMIDI · HELP")
                .setMessage("EDIT MODE\nUse + ADD to create a knob, vertical fader, slider, button, toggle or XY pad. Tap an object to select it; drag to reposition. Drag its white lower-right handle to resize. INSPECT sets dimensions as percentages, colour and MIDI mapping.\n\nPLAY MODE\nPositions are locked. Multiple fingers can operate controls simultaneously. Buttons are momentary, toggles latch, knobs respond to vertical drags.\n\nMIDI\nConnect a USB MIDI device via an OTG adapter, or use an Android-visible virtual MIDI output. Tap MIDI to pick an input port on the destination device. 'Connected' must be shown at the bottom. NTS-1 mk1 requires MIDI RX ShortMessage = ON and the matching channel.\n\nPAGES / PROJECT\nAdd as many pages as needed, rename/duplicate pages, and export/import .json projects. All edits auto-save on the device. The Korg preset covers all 29 documented CCs plus a 12-key keyboard.\n\nSource: Korg NTS-1 mk1 MIDI Implementation Chart (2019).")
                .setPositiveButton("CLOSE",null).show();
    }

    @Override protected void onPause() {
        if(controller!=null)controller.clearPointers();
        saveLayout();
        super.onPause();
    }
}
