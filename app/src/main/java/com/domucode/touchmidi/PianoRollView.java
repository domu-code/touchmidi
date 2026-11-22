package com.domucode.touchmidi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

/**
 * Scrolling, variable-length MIDI piano roll.
 * Tap to add/remove notes. Drag a note to move it, or its right edge to resize it.
 * Drag the top ruler horizontally to scroll; drag the left keyboard vertically.
 */
public final class PianoRollView extends View {
    private static final int BG = 0xff0b1118;
    private static final int TEXT = 0xffdce8ef;
    private static final int MUTED = 0xff8097a6;
    private static final int GRID = 0xff283743;
    private static final int GRID4 = 0xff627e90;
    private static final int GRID8 = 0xff90c1cf;
    private static final int NOTE = 0xff49d8c7;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final MainActivity activity;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<RollModel.Note> sounding = new ArrayList<>();
    private float stepWidth, noteHeight, keyboardWidth, rulerHeight, density;
    private float scrollStep = 0;
    private int topPitch = 83;
    private int activePointer = -1, gesture = 0;
    private static final int NONE = 0, SCROLL_X = 1, SCROLL_Y = 2,
            CREATE = 3, MOVE = 4, RESIZE = 5;
    private float downX, downY, startScroll;
    private int startTopPitch, noteStart, notePitch, noteDuration;
    private RollModel.Note selected;
    private boolean moved, playing;
    private int playStep;
    private long nextTick;

    public PianoRollView(MainActivity activity) {
        super(activity);
        this.activity = activity;
        density = getResources().getDisplayMetrics().density;
        stepWidth = 40 * density;
        noteHeight = 22 * density;
        keyboardWidth = 59 * density;
        rulerHeight = 38 * density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private RollModel roll() { return activity.project().roll; }
    public boolean isPlaying() { return playing; }
    public int currentStep() { return playStep; }
    public void zoom(float factor) {
        stepWidth = Math.max(12 * density, Math.min(120 * density, stepWidth * factor));
        clampScroll();invalidate();
    }

    private void clampScroll() {
        float visible = Math.max(1f,(getWidth()-keyboardWidth)/stepWidth);
        scrollStep = Math.max(0,Math.min(scrollStep,Math.max(0,roll().steps-visible)));
    }

    private void pen(int color, float stroke) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(stroke);
        paint.setStyle(stroke==0?Paint.Style.FILL:Paint.Style.STROKE);
    }

    private void label(Canvas c,String s,float x,float y,float sp,int color,boolean centered) {
        pen(color,0);
        paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        paint.setTextSize(sp*density);
        paint.setTextAlign(centered?Paint.Align.CENTER:Paint.Align.LEFT);
        c.drawText(s,x,y,paint);
    }

    private float gridX(float step) { return keyboardWidth+(step-scrollStep)*stepWidth; }
    private float gridY(int pitch) { return rulerHeight+(topPitch-pitch)*noteHeight; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawColor(BG);
        clampScroll();
        int total=roll().steps;
        int start=Math.max(0,(int)Math.floor(scrollStep));
        int last=Math.min(total,start+(int)Math.ceil((getWidth()-keyboardWidth)/stepWidth)+3);
        float right=getWidth(), bottom=getHeight();

        // Scrolling note canvas, with visible cells only (the sequence can be very long).
        c.save();
        c.clipRect(keyboardWidth,rulerHeight,right,bottom);
        int rowCount=(int)Math.ceil((bottom-rulerHeight)/noteHeight)+1;
        for(int row=0;row<rowCount;row++) {
            int pitch=topPitch-row;
            if(pitch<0)break;
            float y=rulerHeight+row*noteHeight;
            int octave=pitch%12;
            if(octave==1||octave==3||octave==6||octave==8||octave==10) {
                pen(0xff111f29,0);
                c.drawRect(keyboardWidth,y,right,y+noteHeight,paint);
            }
            pen(pitch%12==0?GRID4:GRID,pitch%12==0?1.7f*density:.7f*density);
            c.drawLine(keyboardWidth,y,right,y,paint);
        }
        for(int st=start;st<=last;st++) {
            float x=gridX(st);
            int color=st%8==0?GRID8:st%4==0?GRID4:GRID;
            float width=st%8==0?2.7f:st%4==0?1.5f:.65f;
            pen(color,width*density);
            c.drawLine(x,rulerHeight,x,bottom,paint);
        }
        for(RollModel.Note n:roll().notes) {
            if(n.step+n.duration<start||n.step>last)continue;
            if(n.pitch>topPitch || n.pitch<topPitch-rowCount)continue;
            float x=gridX(n.step), y=gridY(n.pitch);
            RectF b=new RectF(x+1.2f*density,y+1.1f*density,
                    gridX(n.step+n.duration)-1.3f*density,y+noteHeight-1.3f*density);
            if(b.width()<1)continue;
            int fill=(n==selected)?0xffb9fff2:NOTE;
            pen(fill,0);
            c.drawRoundRect(b,3*density,3*density,paint);
            pen(0xff0e4448,0);
            float handleWidth=Math.min(6*density,b.width()*.18f);
            c.drawRect(b.right-handleWidth,b.top,b.right,b.bottom,paint);
        }
        // Readable moving playhead.
        if(playing) {
            float x=gridX(playStep);
            if(x>=keyboardWidth&&x<=right) {
                pen(0xffffba69,2.8f*density);
                c.drawLine(x,rulerHeight,x,bottom,paint);
            }
        }
        c.restore();

        // Timeline ruler, scroll by dragging here.
        pen(0xff192b37,0);c.drawRect(0,0,right,rulerHeight,paint);
        for(int st=start;st<=last;st++) {
            if(st%4!=0)continue;
            float x=gridX(st);
            if(x<keyboardWidth||x>right)continue;
            pen(st%8==0?GRID8:GRID4,(st%8==0?2.2f:1f)*density);
            c.drawLine(x,rulerHeight-9*density,x,rulerHeight,paint);
            label(c,Integer.toString(st+1),x+3*density,15*density,9,st%8==0?TEXT:MUTED,false);
            if(st%16==0)
                label(c,"BAR "+(st/16+1),x+3*density,28*density,8,NOTE,false);
        }
        pen(GRID8,1.5f*density);
        c.drawLine(0,rulerHeight,right,rulerHeight,paint);

        // Fixed piano-key strip; dragging here scrolls the pitch range.
        int rows=(int)Math.ceil((bottom-rulerHeight)/noteHeight)+1;
        for(int row=0;row<rows;row++) {
            int pitch=topPitch-row;
            if(pitch<0)break;
            float y=rulerHeight+row*noteHeight;
            int chroma=pitch%12;
            boolean black=chroma==1||chroma==3||chroma==6||chroma==8||chroma==10;
            pen(black?0xff192a34:0xffd6dfe4,0);
            c.drawRect(0,y,keyboardWidth-2*density,y+noteHeight-1*density,paint);
            if(pitch%12==0)
                label(c,"C"+(pitch/12-1),5*density,y+noteHeight*.75f,10,0xff14212b,false);
            else if(!black&&pitch%12==7)
                label(c,"G"+(pitch/12-1),5*density,y+noteHeight*.75f,8,0xff435866,false);
        }
        pen(0xff355161,1.5f*density);
        c.drawLine(keyboardWidth-1*density,0,keyboardWidth-1*density,bottom,paint);
        label(c,"NOTES",6*density,14*density,9,TEXT,false);
        label(c,"STEPS",6*density,28*density,8,NOTE,false);

        // Total length is independent of the visible/zoomed portion.
        label(c,total+" STEPS",right-7*density,28*density,10,TEXT,true);
    }

    private int pitchAt(float y) {
        return Math.max(0,Math.min(127,topPitch-(int)Math.floor((y-rulerHeight)/noteHeight)));
    }

    private int stepAt(float x) {
        return Math.max(0,Math.min(roll().steps-1,
                (int)Math.floor(scrollStep+(x-keyboardWidth)/stepWidth)));
    }

    private RollModel.Note hitNote(int step,int pitch) {
        ArrayList<RollModel.Note> notes=roll().notes;
        for(int i=notes.size()-1;i>=0;i--) {
            RollModel.Note n=notes.get(i);
            if(n.pitch==pitch && step>=n.step && step<n.step+n.duration)return n;
        }
        return null;
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN) {
            activePointer=e.getPointerId(0);
            downX=e.getX();downY=e.getY();
            moved=false;selected=null;
            if(downY<rulerHeight) {
                gesture=SCROLL_X;startScroll=scrollStep;
            } else if(downX<keyboardWidth) {
                gesture=SCROLL_Y;startTopPitch=topPitch;
            } else {
                if(playing)stop();
                int step=stepAt(downX),pitch=pitchAt(downY);
                selected=hitNote(step,pitch);
                if(selected==null) {
                    selected=new RollModel.Note(step,pitch,1,roll().velocity);
                    roll().notes.add(selected);
                    gesture=CREATE;
                } else {
                    gesture=(selected.step+selected.duration-step<=1
                            && selected.duration>1)?RESIZE:MOVE;
                }
                noteStart=selected.step;notePitch=selected.pitch;
                noteDuration=selected.duration;
            }
            invalidate();return true;
        }
        if(action==MotionEvent.ACTION_MOVE) {
            if(activePointer<0)return true;
            int ix=e.findPointerIndex(activePointer);
            if(ix<0)return true;
            float x=e.getX(ix),y=e.getY(ix);
            float dx=x-downX,dy=y-downY;
            if(Math.abs(dx)>5*density||Math.abs(dy)>5*density)moved=true;
            switch(gesture) {
                case SCROLL_X:
                    scrollStep=startScroll-dx/stepWidth;
                    clampScroll();
                    break;
                case SCROLL_Y:
                    topPitch=Math.max(0,Math.min(127,
                            startTopPitch+Math.round(dy/noteHeight)));
                    break;
                case CREATE:
                    if(selected!=null) {
                        int last=stepAt(x);
                        if(last<noteStart) {
                            selected.step=last;
                            selected.duration=noteStart-last+1;
                        } else {
                            selected.step=noteStart;
                            selected.duration=last-noteStart+1;
                        }
                    }
                    break;
                case RESIZE:
                    if(selected!=null)selected.duration=Math.max(1,
                            Math.min(roll().steps-noteStart,
                                    noteDuration+Math.round(dx/stepWidth)));
                    break;
                case MOVE:
                    if(selected!=null && moved) {
                        selected.step=Math.max(0,Math.min(roll().steps-selected.duration,
                                noteStart+Math.round(dx/stepWidth)));
                        selected.pitch=Math.max(0,Math.min(127,
                                notePitch-Math.round(dy/noteHeight)));
                    }
                    break;
            }
            invalidate();return true;
        }
        if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL) {
            if(action==MotionEvent.ACTION_UP) {
                if(gesture==MOVE&&!moved&&selected!=null)roll().notes.remove(selected);
                if(gesture==SCROLL_X||gesture==SCROLL_Y){} // Ruler and keyboard only scroll.
                if(gesture==CREATE||gesture==MOVE||gesture==RESIZE)activity.saveLayout();
            }
            selected=null;gesture=NONE;activePointer=-1;invalidate();return true;
        }
        return true;
    }

    private final Runnable playbackTick=new Runnable() {
        @Override public void run() {
            if(!playing)return;
            RollModel r=roll();
            if(r.steps<1) {stop();return;}
            int step=playStep%r.steps;
            for(int i=sounding.size()-1;i>=0;i--) {
                RollModel.Note note=sounding.get(i);
                if((note.step+note.duration)%r.steps==step) {
                    activity.midi().noteOff(r.channel,note.pitch);
                    sounding.remove(i);
                }
            }
            for(RollModel.Note note:r.notes) {
                if(note.step==step) {
                    activity.midi().noteOn(r.channel,note.pitch,note.velocity);
                    sounding.add(note);
                }
            }
            playStep=(step+1)%r.steps;
            invalidate();
            long spacing=Math.max(20,Math.round(15000f/r.bpm));
            nextTick+=spacing;
            long now=SystemClock.uptimeMillis();
            if(nextTick<now)nextTick=now+spacing;
            handler.postAtTime(this,nextTick);
        }
    };

    public void togglePlayback() {
        if(playing)stop();
        else {
            playing=true;
            playStep=0;
            nextTick=SystemClock.uptimeMillis();
            handler.post(playbackTick);
            activity.onRollPlaybackChanged();
        }
    }

    public void stop() {
        handler.removeCallbacks(playbackTick);
        for(RollModel.Note n:sounding)
            activity.midi().noteOff(roll().channel,n.pitch);
        sounding.clear();
        playing=false;playStep=0;
        activity.onRollPlaybackChanged();
        invalidate();
    }

    public void projectChanged() {
        stop();
        scrollStep=0;topPitch=83;
        invalidate();
    }
}
