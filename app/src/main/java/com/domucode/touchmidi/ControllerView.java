package com.domucode.touchmidi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public final class ControllerView extends View {
    private final MainActivity activity;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SparseArray<Model.Control> touching = new SparseArray<>();
    private final SparseArray<Float> knobStartValue = new SparseArray<>();
    private final SparseArray<Float> knobStartY = new SparseArray<>();
    private Model.Control selected;
    private Model.Control edited;
    private int editPointerId = -1;
    private boolean resizing;
    private float dragX, dragY, originalX, originalY, originalW, originalH;
    private final float density;
    private static final int BG = 0xff0b1118;
    private static final int FACE = 0xff17232d;
    private static final int WHITE = 0xffe2eaf0;
    private static final int MUTED = 0xff91a5b4;

    public ControllerView(MainActivity context) {
        super(context);
        activity = context;
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public Model.Control getSelected() { return selected; }
    public void select(Model.Control c) { selected = c; invalidate(); }
    public void clearPointers() {
        for (int i = 0; i < touching.size(); i++) {
            Model.Control c = touching.valueAt(i);
            if ("BUTTON".equals(c.kind)) activity.midi().release(c);
        }
        touching.clear(); knobStartValue.clear(); knobStartY.clear();
        edited = null; editPointerId = -1; invalidate();
    }

    private float px(float fraction) { return fraction * getWidth(); }
    private float py(float fraction) { return fraction * getHeight(); }

    private RectF bounds(Model.Control c) {
        return new RectF(px(c.x), py(c.y), px(c.x + c.w), py(c.y + c.h));
    }

    private void color(int color, Paint.Style style, float width) {
        p.reset(); p.setAntiAlias(true); p.setColor(color);
        p.setStyle(style); p.setStrokeWidth(width);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }

    private int translucent(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private float dp(float n) { return n * density; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(BG);
        if (activity.isEditing()) {
            color(0xff1e2a32, Paint.Style.STROKE, dp(.7f));
            for (int x = 0; x < getWidth(); x += Math.max(1,(int)(getWidth()/20f)))
                canvas.drawLine(x, 0, x, getHeight(), p);
            for (int y = 0; y < getHeight(); y += Math.max(1,(int)(getHeight()/12f)))
                canvas.drawLine(0, y, getWidth(), y, p);
        }

        ArrayList<Model.Control> all = activity.project().page().controls;
        if (all.isEmpty()) {
            color(MUTED, Paint.Style.FILL, 0);
            p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            p.setTextAlign(Paint.Align.CENTER); p.setTextSize(dp(17));
            canvas.drawText(activity.isEditing() ? "ADD CONTROLS TO THIS PAGE" : "EMPTY PAGE · SWITCH TO EDIT",
                    getWidth()/2f, getHeight()/2f, p);
        }
        for (Model.Control c : all) drawControl(canvas, c);
    }

    private void drawControl(Canvas canvas, Model.Control c) {
        RectF b = bounds(c);
        float radius = Math.min(dp(12), Math.min(b.width(),b.height())*.12f);
        color(FACE, Paint.Style.FILL, 0);
        canvas.drawRoundRect(b, radius, radius, p);
        color(translucent(c.color, 95), Paint.Style.STROKE, dp(1.5f));
        canvas.drawRoundRect(b, radius, radius, p);

        boolean pressed = isTouched(c);
        String kind = c.kind;
        float cx = b.centerX(), cy = b.centerY();
        float labelSize = Math.max(dp(9), Math.min(dp(15), b.width()*.115f));
        float smallSize = Math.max(dp(8), Math.min(dp(11), b.width()*.085f));
        float top = b.top + Math.min(dp(22), b.height()*.18f);

        if ("KNOB".equals(kind)) {
            float r = Math.min(b.width()*.29f, b.height()*.26f);
            float knobY = b.top + b.height()*.53f;
            RectF ring = new RectF(cx-r, knobY-r, cx+r, knobY+r);
            color(0xff384c5a, Paint.Style.STROKE, Math.max(dp(4),r*.16f));
            canvas.drawArc(ring, 135, 270, false, p);
            color(c.color, Paint.Style.STROKE, Math.max(dp(4),r*.16f));
            canvas.drawArc(ring, 135, c.fraction()*270, false, p);
            color(0xff233543, Paint.Style.FILL, 0);
            canvas.drawCircle(cx, knobY, r*.68f, p);
            double theta = Math.toRadians(135 + c.fraction()*270);
            color(c.color, Paint.Style.STROKE, Math.max(dp(2),r*.09f));
            canvas.drawLine(cx, knobY, cx+(float)Math.cos(theta)*r*.55f,
                    knobY+(float)Math.sin(theta)*r*.55f,p);
            drawText(canvas, fit(c.displayValue(),b.width()*.9f,smallSize),cx,
                    b.top+b.height()*.84f, smallSize, WHITE,true);
        } else if ("FADER".equals(kind)) {
            float start = b.top + b.height()*.30f;
            float end = b.bottom - b.height()*.19f;
            color(0xff374c58, Paint.Style.STROKE, dp(5));
            canvas.drawLine(cx,start,cx,end,p);
            float thumbY = end-(end-start)*c.fraction();
            color(c.color, Paint.Style.STROKE,dp(5));
            canvas.drawLine(cx,thumbY,cx,end,p);
            color(c.color, Paint.Style.FILL,0);
            canvas.drawRoundRect(cx-dp(15),thumbY-dp(5),cx+dp(15),thumbY+dp(5),dp(4),dp(4),p);
            drawText(canvas, c.displayValue(),cx,b.bottom-dp(8),smallSize,WHITE,true);
        } else if ("SLIDER".equals(kind)) {
            float left=b.left+Math.min(dp(18),b.width()*.12f);
            float right=b.right-Math.min(dp(18),b.width()*.12f);
            float trackY=b.top+b.height()*.56f;
            color(0xff374c58,Paint.Style.STROKE,dp(5));
            canvas.drawLine(left,trackY,right,trackY,p);
            float thumbX=left+(right-left)*c.fraction();
            color(c.color,Paint.Style.STROKE,dp(5));
            canvas.drawLine(left,trackY,thumbX,trackY,p);
            color(c.color,Paint.Style.FILL,0);
            canvas.drawRoundRect(thumbX-dp(5),trackY-dp(15),thumbX+dp(5),trackY+dp(15),dp(4),dp(4),p);
            drawText(canvas,c.displayValue(),cx,b.bottom-dp(9),smallSize,WHITE,true);
        } else if ("XY".equals(kind)) {
            RectF pad = new RectF(b.left+dp(12),b.top+b.height()*.31f,
                    b.right-dp(12),b.bottom-dp(15));
            color(0xff233542,Paint.Style.FILL,0);
            canvas.drawRoundRect(pad,dp(5),dp(5),p);
            float crossX=pad.left+(pad.width()*c.value/127f);
            float crossY=pad.bottom-(pad.height()*c.value2/127f);
            color(translucent(c.color,120),Paint.Style.STROKE,dp(1));
            canvas.drawLine(crossX,pad.top,crossX,pad.bottom,p);
            canvas.drawLine(pad.left,crossY,pad.right,crossY,p);
            color(c.color,Paint.Style.FILL,0);
            canvas.drawCircle(crossX,crossY,dp(6),p);
        } else {
            float pad=Math.min(dp(12),Math.min(b.width(),b.height())*.12f);
            RectF inner=new RectF(b.left+pad,b.top+b.height()*.29f,
                    b.right-pad,b.bottom-b.height()*.19f);
            boolean on=pressed || ("TOGGLE".equals(kind) && c.state);
            color(on ? translucent(c.color,210) : 0xff2a3a45, Paint.Style.FILL,0);
            canvas.drawRoundRect(inner,dp(8),dp(8),p);
            drawText(canvas, "TOGGLE".equals(kind) ? (c.state?"ON":"OFF") : "PUSH",
                    cx,inner.centerY()+dp(4), Math.max(dp(9),Math.min(dp(15),inner.width()*.13f)),
                    on? BG : MUTED,true);
        }
        drawText(canvas,fit(c.label,b.width()*.89f,labelSize),cx,top,labelSize,WHITE,true);
        String midiText = "CC".equals(c.message) ? "CC " + c.number
                : "NOTE".equals(c.message) ? "NOTE " + c.number
                : "PROGRAM".equals(c.message) ? "PROGRAM " + c.number
                : c.message;
        drawText(canvas,fit(midiText + " · CH" + c.channel,b.width()*.9f,smallSize),
                cx,b.bottom-dp(5),smallSize,MUTED,true);

        if (activity.isEditing() && selected == c) {
            color(0xfff6f8fa,Paint.Style.STROKE,dp(2));
            canvas.drawRoundRect(b, radius,radius,p);
            float s=dp(17);
            color(0xfff6f8fa,Paint.Style.FILL,0);
            canvas.drawRoundRect(b.right-s,b.bottom-s,b.right+dp(1),b.bottom+dp(1),dp(3),dp(3),p);
            color(BG,Paint.Style.STROKE,dp(1.4f));
            canvas.drawLine(b.right-s*.62f,b.bottom-dp(4),b.right-dp(4),b.bottom-s*.62f,p);
        }
    }

    private void drawText(Canvas canvas,String text,float x,float baseline,float size,int col,boolean centered) {
        color(col,Paint.Style.FILL,0);
        p.setTextSize(size);
        p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        if (centered) p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text,x,baseline,p);
    }

    private String fit(String text,float maxWidth,float size) {
        color(WHITE,Paint.Style.FILL,0); p.setTextSize(size);
        if (p.measureText(text)<=maxWidth) return text;
        while (text.length()>1 && p.measureText(text+"…")>maxWidth)
            text=text.substring(0,text.length()-1);
        return text+"…";
    }

    private boolean isTouched(Model.Control c) {
        for(int i=0;i<touching.size();i++) if(touching.valueAt(i)==c) return true;
        return false;
    }

    private Model.Control hit(float x,float y) {
        ArrayList<Model.Control> all=activity.project().page().controls;
        for(int i=all.size()-1;i>=0;i--) {
            RectF b=bounds(all.get(i));
            if(b.contains(x,y)) return all.get(i);
        }
        return null;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action=event.getActionMasked(), index=event.getActionIndex();
        if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN)
            fingerDown(event.getPointerId(index),event.getX(index),event.getY(index));
        else if(action==MotionEvent.ACTION_MOVE) {
            for(int i=0;i<event.getPointerCount();i++)
                fingerMove(event.getPointerId(i),event.getX(i),event.getY(i));
        } else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_POINTER_UP) {
            fingerUp(event.getPointerId(index));
        } else if(action==MotionEvent.ACTION_CANCEL) {
            clearPointers(); activity.saveLayout();
        }
        invalidate();
        return true;
    }

    private void fingerDown(int id,float x,float y) {
        Model.Control c=hit(x,y);
        if(activity.isEditing()) {
            if(editPointerId>=0) return;
            selected=c; edited=c; editPointerId=id;
            dragX=x;dragY=y;
            if(c!=null) {
                originalX=c.x;originalY=c.y;originalW=c.w;originalH=c.h;
                RectF b=bounds(c);
                resizing=x>=b.right-dp(28)&&y>=b.bottom-dp(28);
            }
            activity.onSelectionChanged();
            return;
        }
        if(c==null) return;
        if("TOGGLE".equals(c.kind)) {
            c.state=!c.state;
            if(c.state) activity.midi().send(c,c.max);
            else activity.midi().release(c);
            activity.saveLayout();
            return;
        }
        touching.put(id,c);
        knobStartY.put(id,y);
        knobStartValue.put(id,c.fraction());
        if("BUTTON".equals(c.kind)) {
            activity.midi().send(c, "NOTE".equals(c.message) ? Math.max(1,c.value) : c.max);
        } else {
            updateContinuous(c,id,x,y);
        }
    }

    private void fingerMove(int id,float x,float y) {
        if(activity.isEditing()) {
            if(id!=editPointerId || edited==null)return;
            float dx=(x-dragX)/Math.max(1,getWidth());
            float dy=(y-dragY)/Math.max(1,getHeight());
            if(resizing) {
                edited.w=Math.max(.055f,Math.min(1-originalX,originalW+dx));
                edited.h=Math.max(.09f,Math.min(1-originalY,originalH+dy));
            } else {
                edited.x=originalX+dx;
                edited.y=originalY+dy;
            }
            edited.normalize();
            return;
        }
        Model.Control c=touching.get(id);
        if(c!=null && !"BUTTON".equals(c.kind)) updateContinuous(c,id,x,y);
    }

    private void fingerUp(int id) {
        if(activity.isEditing()) {
            if(editPointerId==id) {
                if(edited!=null) {
                    if(activity.snapEnabled()) {
                        float gridX=.025f,gridY=.04f;
                        edited.x=Math.round(edited.x/gridX)*gridX;
                        edited.y=Math.round(edited.y/gridY)*gridY;
                        if(resizing) {
                            edited.w=Math.max(.055f,Math.round(edited.w/gridX)*gridX);
                            edited.h=Math.max(.09f,Math.round(edited.h/gridY)*gridY);
                        }
                        edited.normalize();
                    }
                    activity.saveLayout();
                }
                edited=null;editPointerId=-1;
            }
            return;
        }
        Model.Control c=touching.get(id);
        if(c!=null && "BUTTON".equals(c.kind)) activity.midi().release(c);
        touching.remove(id);knobStartY.remove(id);knobStartValue.remove(id);
        activity.saveLayout();
    }

    private void updateContinuous(Model.Control c,int id,float x,float y) {
        RectF b=bounds(c);
        float ratio;
        if("KNOB".equals(c.kind)) {
            Float sy=knobStartY.get(id), base=knobStartValue.get(id);
            ratio=(base==null?.5f:base)+(sy==null?0:(sy-y)/Math.max(dp(80),b.height())*1.4f);
        } else if("FADER".equals(c.kind)) {
            float start=b.top+b.height()*.3f,end=b.bottom-b.height()*.19f;
            ratio=(end-y)/Math.max(1,end-start);
        } else if("XY".equals(c.kind)) {
            float left=b.left+dp(12),right=b.right-dp(12);
            float top=b.top+b.height()*.31f,bottom=b.bottom-dp(15);
            ratio=(x-left)/Math.max(1,right-left);
            int v2=Math.max(0,Math.min(127,Math.round(127*(bottom-y)/Math.max(1,bottom-top))));
            if(c.value2!=v2) { c.value2=v2; activity.midi().sendSecondary(c,v2); }
        } else {
            float left=b.left+Math.min(dp(18),b.width()*.12f);
            float right=b.right-Math.min(dp(18),b.width()*.12f);
            ratio=(x-left)/Math.max(1,right-left);
        }
        int newValue=c.fromFraction(ratio);
        if(c.value!=newValue) {
            c.value=newValue;
            activity.midi().send(c,newValue);
        }
    }
}
